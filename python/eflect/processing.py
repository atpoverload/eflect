""" code used to process data collected by eflect. """
import os

from argparse import ArgumentParser

import numpy as np
import pandas as pd

from pandas import to_datetime

from protos.sample.sample_pb2 import DataSet

# processing helpers
# TODO(timur): this is totally arbitrary. do we need metadata in the proto?
SAMPLE_INTERVAL = '50ms'
WINDOW_SIZE = '501ms'


def bucket_timestamps(timestamps, sample_interval=SAMPLE_INTERVAL):
    """ Floors a series of timestamps to some interval for easy aggregates. """
    return to_datetime(timestamps).dt.floor(sample_interval)


def max_rolling_difference(df, window_size=WINDOW_SIZE):
    """ Computes a rolling difference of points up to the window size. """
    values = df - df.rolling(window_size).min()

    timestamps = df.reset_index().timestamp.astype(int) / 10**9
    timestamps.index = df.index
    timestamps = timestamps - timestamps.rolling(window_size).min()

    return values, timestamps


# jiffies processing
def parse_cpu_samples(samples):
    """ Converts a collection of CpuSample to a DataFrame. """
    records = []
    for sample in samples:
        for stat in sample.reading:
            records.append([
                sample.timestamp,
                stat.cpu,
                stat.user,
                stat.nice,
                stat.system,
                stat.idle,
                stat.iowait,
                stat.irq,
                stat.softirq,
                stat.steal,
                stat.guest,
                stat.guest_nice
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'cpu',
        'user',
        'nice',
        'system',
        'idle',
        'iowait',
        'irq',
        'softirq',
        'steal',
        'guest',
        'guest_nice'
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


def process_cpu_data(df):
    """ Computes the cpu jiffy rate of each 50ms bucket """
    df['jiffies'] = df.drop(
        columns=['timestamp', 'cpu', 'idle', 'iowait']).sum(axis=1)
    df.timestamp = bucket_timestamps(df.timestamp)

    jiffies, ts = max_rolling_difference(df.groupby(
        ['timestamp', 'cpu']).jiffies.min().unstack())
    jiffies = jiffies.stack().reset_index()
    jiffies = jiffies.groupby(['timestamp', 'cpu']).sum().unstack()
    jiffies = jiffies.div(ts, axis=0).stack()

    return jiffies[0]


def cpu_samples_to_df(samples):
    """ Converts a collection of CpuSamples to a processed DataFrame. """
    return process_cpu_data(parse_cpu_samples(samples))


def parse_task_samples(samples):
    """ Converts a collection of TaskSamples to a DataFrame. """
    records = []
    for sample in samples:
        for stat in sample.reading:
            records.append([
                sample.timestamp,
                stat.task_id,
                # stat.task_name,
                stat.cpu,
                stat.user,
                stat.system
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'id',
        # 'name',
        'cpu',
        'user',
        'system',
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


def process_task_data(df):
    """ Computes the app jiffy rate of each 50ms bucket """
    df['jiffies'] = df.user + df.system
    # TODO(timur): the thread name is currently unused because it typically isn't useful
    # df = df[~df.name.str.contains('eflect-')]
    # df['id'] = df.id.astype(str) + '-' + df.name

    df.timestamp = bucket_timestamps(df.timestamp)
    cpu = df.groupby(['timestamp', 'id']).cpu.max()

    jiffies, ts = max_rolling_difference(df.groupby(
        ['timestamp', 'id']).jiffies.min().unstack())
    jiffies = jiffies.stack().to_frame()
    jiffies['cpu'] = cpu
    jiffies = jiffies.groupby(['timestamp', 'id', 'cpu'])[0].sum(
    ).unstack().unstack().div(ts, axis=0).stack().stack(0)

    return jiffies


def task_samples_to_df(samples):
    """ Converts a collection of TaskSamples to a processed DataFrame. """
    return process_task_data(parse_task_samples(samples))


# battery_manager processing
def parse_battery_manager_samples(samples):
    """ Converts a collection of BatteryManagerSamples to a DataFrame. """
    records = []
    for sample in samples:
        for reading in sample.reading:
            records.append([
                sample.timestamp,
                reading.battery_property_energy_counter,
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'energy',
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


def process_battery_manager_data(df):
    """ Computes the power of each 50ms bucket """
    df.timestamp = bucket_timestamps(df.timestamp)
    df = df.groupby(['timestamp']).min()

    energy, ts = max_rolling_difference(df)
    energy = energy.div(ts, axis=0)

    return energy


def battery_manager_samples_to_df(samples):
    """ Converts a collection of BatteryManagerSamples to a processed DataFrame. """
    return process_battery_manager_data(parse_battery_manager_samples(samples))


# nvml processing
def parse_nvml_samples(samples):
    """ Converts a collection of RaplSamples to a DataFrame. """
    records = []
    for sample in samples:
        for reading in sample.reading:
            records.append([
                sample.timestamp,
                reading.bus_id,
                reading.power_usage,
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'bus_id',
        'power_usage',
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


def process_nvml_data(df):
    """ Computes the power of each 50ms bucket """
    df.timestamp = bucket_timestamps(df.timestamp)
    df = df.groupby(['timestamp', 'bus_id']).min()
    # TODO(timur): this assumes the baseline is the smallest reading; we should do something else
    df = df - df.groupby('bus_id').min()

    return df


def nvml_samples_to_df(samples):
    """ Converts a collection of NvmlSamples to a processed DataFrame. """
    return process_nvml_data(parse_nvml_samples(samples))


# rapl processing
WRAP_AROUND_VALUE = 16384


def parse_rapl_samples(samples):
    """ Converts a collection of RaplSamples to a DataFrame. """
    records = []
    for sample in samples:
        for reading in sample.reading:
            records.append([
                sample.timestamp,
                reading.socket,
                reading.cpu,
                reading.package,
                reading.dram,
                reading.gpu
            ])
    df = pd.DataFrame(records)
    df.columns = [
        'timestamp',
        'socket',
        'cpu',
        'package',
        'dram',
        'gpu'
    ]
    df.timestamp = pd.to_datetime(df.timestamp, unit='ms')
    return df


# TODO(timur): i've been told by alejandro that the value i'm using isn't
#   actually the wrap around. i'm not sure how to look it up properly however.
def maybe_apply_wrap_around(value):
    """ Checks if the value needs to be adjusted by the wrap around. """
    if value < 0:
        return value + WRAP_AROUND_VALUE
    else:
        return value


def process_rapl_data(df):
    """ Computes the power of each 50ms bucket """
    df.timestamp = bucket_timestamps(df.timestamp)
    df = df.groupby(['timestamp', 'socket']).min()
    df.columns.name = 'component'

    energy, ts = max_rolling_difference(df.unstack())
    energy = energy.stack().stack().apply(maybe_apply_wrap_around)
    energy = energy.groupby(
        ['timestamp', 'socket', 'component']).sum().div(ts, axis=0)

    return energy


def rapl_samples_to_df(samples):
    """ Converts a collection of RaplSamples to a processed DataFrame. """
    return process_rapl_data(parse_rapl_samples(samples))


# accounting
def account_jiffies(task, cpu):
    """ Returns the ratio of the jiffies with an overaccounting correction. """
    task = task_samples_to_df(task)
    cpu = cpu_samples_to_df(cpu)
    # TODO(timur): let's clean this; i think it's outputting some garbage data
    return (task / cpu.replace(0, 1)).replace(np.inf, 1).clip(0, 1)


def account_battery_manager_energy(activity, battery_manager):
    """ Returns the product of energy and activity by socket. """
    activity = activity.reset_index()
    activity = activity.set_index(['timestamp', 'id'])['activity']

    battery_manager = battery_manager_samples_to_df(battery_manager)

    # TODO(timur): we should just be able to take the product but the axis
    #   misalignment causes it to fail sometimes
    try:
        df = battery_manager * activity
    except:
        print('battery_manager data could not be directly aligned; forced merge instead')
        activity = activity.reset_index()
        battery_manager = battery_manager.reset_index()
        df = pd.merge(activity, battery_manager, on=['timestamp'])
        df[0] = df['activity'] * df['energy']
        df = df.set_index(['timestamp', 'id'])[0]

    df = df.reset_index().set_index(['timestamp', 'id'])
    df.name = 'power'
    return df


# TODO(timur): this is an incomplete way of doing this. can we look up the
# thread positioning within the gpu with nvml?
def account_nvml_energy(activity, nvml):
    """ Returns the product of energy and activity. """
    activity = activity.groupby(['timestamp', 'id']).sum()

    nvml = nvml_samples_to_df(nvml)

    # TODO(timur): we should just be able to take the product but the axis
    #   misalignment causes it to fail sometimes
    try:
        df = nvml * activity
    except:
        print('nvml data could not be directly aligned; forced merge instead')
        activity = activity.reset_index()
        nvml = nvml.reset_index()
        df = pd.merge(activity, nvml, on=['timestamp'])
        df[0] = df['activity'] * df['power_usage']
        df = df.set_index(['timestamp', 'id'])[0]

    df = df.reset_index().set_index(['timestamp', 'id'])
    df.name = 'power'
    return df


# TODO(timur): find out if there's a way to abstract this
def RAPL_DOMAIN_CONVERSION(x): return 0 if int(x) < 20 else 1


def account_rapl_energy(activity, rapl):
    """ Returns the product of energy and activity by socket. """
    activity = activity.reset_index()
    activity['socket'] = activity.cpu.apply(RAPL_DOMAIN_CONVERSION)
    activity = activity.set_index(['timestamp', 'id', 'socket'])['activity']

    rapl = rapl_samples_to_df(rapl)

    # TODO(timur): we should just be able to take the product but the axis
    #   misalignment causes it to fail sometimes
    try:
        df = rapl * activity
    except:
        print('rapl data could not be directly aligned; forced merge instead')
        activity = activity.reset_index()
        rapl = rapl.reset_index()
        df = pd.merge(activity, rapl, on=['timestamp', 'socket'])
        df[0] = df['0_x'] * df['0_y']
        df = df.set_index(['timestamp', 'id', 'component', 'socket'])[0]

    df = df.reset_index().set_index(['timestamp', 'id', 'component', 'socket'])
    df.name = 'power'
    return df


def compute_footprint(data):
    """ Produces an energy footprint from the data set. """
    # TODO(timur): we need a summary proto for this
    activity = account_jiffies(data.task, data.cpu)
    activity.name = 'activity'
    footprints = {'activity': activity}

    if len(data.battery_manager) > 0:
        footprints['battery_manager'] = account_battery_manager_energy(
            activity, data.battery_manager)

    if len(data.nvml) > 0:
        footprints['nvml'] = account_nvml_energy(activity, data.nvml)

    if len(data.rapl) > 0:
        footprints['rapl'] = account_rapl_energy(activity, data.rapl)

    return footprints


# cli to process globs of files
def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        dest='files',
        nargs='*',
        default=None,
        help='files to process',
    )
    parser.add_argument(
        '-o',
        '--output_dir',
        dest='output',
        default=None,
        help='directory to write the processed data to',
    )
    return parser.parse_args()


def main():
    args = parse_args()
    for file in args.files:
        with open(file, 'rb') as f:
            data = DataSet()
            data.ParseFromString(f.read())

        # TODO(timur): i hate that i did this. we need to get the footprint(s) in a proto
        if args.output:
            if os.path.exists(args.output) and not os.path.isdir(args.output):
                raise RuntimeError(
                    'output target {} already exists and is not a directory; aborting'.format(args.output))
            elif not os.path.exists(args.output):
                os.makedirs(args.output)

            path = os.path.join(args.output, os.path.splitext(
                os.path.basename(file))[0]) + '.zip'
        else:
            path = os.path.splitext(file)[0] + '.zip'
        footprints = compute_footprint(data)

        for key in footprints:
            footprints[key].to_csv(
                path,
                compression=dict(
                    method='zip',
                    archive_name='{}.csv'.format(key)
                )
            )


if __name__ == '__main__':
    main()

import os

from sys import argv

import numpy as np
import pandas as pd

from tqdm import tqdm

# constants for vaporeon/jolteon experiments
HOT_ITERS_RATIO = 5
WRAP_AROUND_VALUE = 16384
SAMPLE_INTERVAL = '50ms'
WINDOW_SIZE = '501ms'
DOMAIN_CONVERSION = lambda x: 0 if int(x) < 20 else 1

def to_timestamp(timestamps):
    return pd.to_datetime(timestamps).dt.floor(SAMPLE_INTERVAL)

def max_rolling_difference(df, window_size = WINDOW_SIZE):
    values = df - df.rolling(window_size).min()

    timestamps = df.reset_index().timestamp.astype(int) / 10**9
    timestamps.index = df.index
    timestamps = timestamps - timestamps.rolling(window_size).min()

    return values, timestamps

def check_wrap_around(value):
    if value < 0:
        return value + WRAP_AROUND_VALUE
    else:
        return value

def read_energy_data(path):
    df = pd.read_csv(path, header = None)
    df.columns = ['timestamp', 'domain', 'dram', 'cpu', 'package']
    df.timestamp = to_timestamp(df.timestamp)
    df = df.groupby(['timestamp', 'domain']).min()
    df.columns.name = 'component'

    energy, ts = max_rolling_difference(df.unstack())
    energy = energy.stack().stack().apply(check_wrap_around)
    energy = energy.groupby(['timestamp', 'domain']).sum().unstack().div(ts, axis = 0).stack()

    return energy

def read_full_cpu_data(path):
    df = pd.read_csv(path, header = None)
    df.columns = ['timestamp', 'cpu', 'user', 'nice', 'system', 'irq', 'softirq', 'steal', 'guest', 'guest_nice']

    df.timestamp = to_timestamp(df.timestamp)

    jiffies, ts = max_rolling_difference(df.groupby(['timestamp', 'cpu']).min().unstack())
    jiffies = jiffies.stack().reset_index()
    jiffies['domain'] = jiffies.cpu.apply(DOMAIN_CONVERSION)
    jiffies = jiffies.groupby(['timestamp', 'domain']).sum().unstack()
    jiffies = jiffies.div(ts, axis = 0).stack()

    return jiffies.drop(columns = ['cpu'])

def read_cpu_data(path):
    df = pd.read_csv(path, header = None)
    df.columns = ['timestamp', 'cpu', 'jiffies']

    df.timestamp = to_timestamp(df.timestamp)

    jiffies, ts = max_rolling_difference(df.groupby(['timestamp', 'cpu']).jiffies.min().unstack())
    jiffies = jiffies.stack().reset_index()
    jiffies['domain'] = jiffies.cpu.apply(DOMAIN_CONVERSION)
    jiffies = jiffies.groupby(['timestamp', 'domain'])[0].sum().unstack()
    jiffies = jiffies.div(ts, axis = 0).stack()

    return jiffies

def read_app_data(path):
    df = pd.read_csv(path, header = None)
    df.columns = ['timestamp', 'id', 'name', 'cpu', 'jiffies']
    df = df[~df.name.str.contains('eflect-')]

    df.timestamp = to_timestamp(df.timestamp)
    df['domain'] = df.cpu.apply(DOMAIN_CONVERSION)
    df['id'] = df.id.astype(str) + '-' + df.name

    jiffies, ts = max_rolling_difference(df.groupby(['timestamp', 'id']).jiffies.min().unstack())
    jiffies = jiffies.stack().to_frame()
    domain = df.groupby(['timestamp', 'id']).domain.max()
    jiffies['domain'] = domain
    jiffies = jiffies.groupby(['timestamp', 'domain'])[0].sum().unstack().div(ts, axis = 0).stack()

    return jiffies

def pre_process(data_dir):
    app = []
    cpu = []
    energy = []
    for run in os.listdir(data_dir):
        hot_iters = len(os.listdir(os.path.join(data_dir, run))) // HOT_ITERS_RATIO
        for i in tqdm(os.listdir(os.path.join(data_dir, run))):
            if int(i) < hot_iters:
                continue
            for f in os.listdir(os.path.join(data_dir, run, i)):
                f = os.path.join(data_dir, run, i, f)
                if 'ProcTaskSample' in f:
                    app.append(read_app_data(f).to_frame().reset_index().assign(run = int(run)).set_index(['timestamp', 'run', 'domain'])[0])
                elif 'FullProcStatSample' in f:
                    # re-integrate this
                    cpu.append(read_full_cpu_data(f).reset_index().assign(run = int(run)).set_index(['timestamp', 'run', 'domain']))
                elif 'ProcStatSample' in f:
                    continue
                    cpu.append(read_cpu_data(f).to_frame().reset_index().assign(run = int(run)).set_index(['timestamp', 'run', 'domain'])[0])
                elif 'EnergySample' in f:
                    energy.append(read_energy_data(f).to_frame().reset_index().assign(run = int(run)).set_index(['timestamp', 'run', 'domain'])[0])

    return pd.concat(app), pd.concat(cpu), pd.concat(energy)

import json
import os
import zipfile

from argparse import ArgumentParser

import pandas as pd


def get_inputs(args):
    return ';'.join([args[k] for k in args if 'input' in k])


def parse_timeline(timeline, timeline_number):
    metadata = [e for e in timeline['traceEvents'] if e['ph'] == 'M']
    devices = {}
    # TODO(timur): find a way to map this gracefully
    for device in metadata:
        device_name = device['args']['name']
        if ':GPU:' in device_name:
            devices[device['pid']] = 'gpu:' + device_name.split(':GPU:')[1].split('/')[0].split(' ')[0]
        elif ':CPU:' in device_name:
            devices[device['pid']] = 'cpu:' + device_name.split(':CPU:')[1].split('/')[0].split(' ')[0]

    events = [e for e in timeline['traceEvents'] if e['ph'] == 'X']
    events = pd.DataFrame.from_dict(events)

    events['timestamp'] = pd.to_datetime(events.ts, unit='us')
    events['layer'] = events.args.map(lambda a: a['name'])
    events['inputs'] = events.args.map(get_inputs)
    events['device'] = events.pid.map(lambda pid: devices[pid])
    events['timeline'] = timeline_number
    events = events.sort_values(by=['timestamp'])

    return events.set_index(['timeline', 'timestamp', 'device', 'layer', 'inputs']).dur


# cli to process globs of files
def parse_args():
    """ Parses timeline alignment arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        dest='data',
        nargs='*',
        default=None,
        help='data directories to align',
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

    for data in args.data:
        print(f'loading data from {data}')
        archive = zipfile.ZipFile(os.path.join(data, 'eflect-data.zip'))
        energy = {file.filename.split(r'.')[0]: pd.read_csv(
            archive.open(file), parse_dates=['timestamp']
        ) for file in archive.filelist}
        # TODO(timur): how do we handle this gracefully?
        rapl = energy['rapl']
        rapl['device'] = 'cpu:' + rapl.socket.astype(str)
        rapl = rapl.groupby(['timestamp', 'device']).energy.sum()

        nvml = energy['nvml']
        # TODO(timur): period metadata?
        dt = pd.Series(nvml.timestamp.unique()).sort_values().diff().min()
        # TODO(timur): maybe an index field in the proto?
        nvml['device'] = 'gpu:0'
        nvml = nvml.groupby(['timestamp', 'device']).energy.sum()

        energy = pd.concat([rapl, nvml])
        print('energy summary:')
        print(energy.groupby('device').agg(('sum', 'mean', 'std')))

        timeline = list(filter(
            lambda file: 'timeline' in file and file.endswith(r'.json'),
            os.listdir(data)
        ))
        print(f'loading {len(timeline)} timelines')
        timeline = pd.concat(list(map(
            lambda f: parse_timeline(
                json.load(open(os.path.join(data, f))),
                int(f.split('-')[-1].split(r'.')[0])
            ),
            timeline
        ))).reset_index()
        timeline.timestamp = timeline.timestamp.dt.floor(dt)

        df = timeline.set_index(
            ['timeline', 'timestamp', 'device', 'layer', 'inputs']).join(energy)
        df['power'] = df.energy * df.dur / df.groupby('timestamp').dur.sum()
        df['energy'] = df.energy * df.dur / 10**9
        df = df[['power', 'energy', 'dur']]

        df.to_csv(os.path.join(data, 'aligned.csv'))


if __name__ == '__main__':
    main()

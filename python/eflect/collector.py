""" A data collector for eflect. Only works with rapl. """
import json
import os

from argparse import ArgumentParser
from concurrent.futures import ProcessPoolExecutor
from multiprocessing import Pipe
from pprint import pprint
from time import sleep, time

# we may be able to prune this
import pyRAPL


# general helper
def get_unixtime():
    return int(time() * 10**3)


# jiffies sources
CPU_JIFFIES_NAMES = [
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

PROC_STAT = os.path.join('/proc', 'stat')


def get_tasks(pid):
    """ Returns pid's current tasks """
    return os.listdir(os.path.join('/proc', str(pid), 'task'))


def get_task_stat_file(pid, tid):
    """ Returns the stat for a task """
    return os.path.join('/proc', str(pid), 'task', str(tid), 'stat')


def parse_task_stat(stat_file):
    """ Returns a task's id, name, cpu, user jiffies, and system jiffies """
    with open(stat_file, 'r') as file:
        stats = file.read().split(' ')
        offset = len(stats) - 52 + 2
        name = " ".join(stats[1:offset])[1:-1]
        return {
            'task_id': int(stats[0]),
            'name': name,
            'cpu': stats[38],
            'user': int(stats[13]),
            'system': int(stats[14])
        }


def sample_process(pid):
    """ Returns the current time and by-task jiffies """
    tasks = [parse_task_stat(get_task_stat_file(pid, task))
             for task in get_tasks(pid)]
    return {'process': {'timestamp': get_unixtime(), 'reading': tasks}}


def sample_cpu():
    """ Returns the current time and by-cpu jiffies """
    cpus = []
    with open(PROC_STAT) as f:
        f.readline()
        for cpu in range(os.cpu_count()):
            jiffies = dict(zip(CPU_JIFFIES_NAMES, f.readline().replace(
                os.linesep, '').split(' ')[1:]))
            jiffies['cpu'] = cpu
            cpus.append(jiffies)
    return {'cpu': {'timestamp': get_unixtime(), 'reading': cpus}}


# rapl sources
MEASUREMENT = None


def sample_rapl():
    """ Gets a reading from the measurement. """
    global MEASUREMENT
    if MEASUREMENT is None:
        pyRAPL.setup()
        pyRAPL.Measurement('bar').begin()
    pyRAPL.Measurement('bar').end()
    energy = pyRAPL.Measurement('bar').result
    energy = [{
        'socket': socket,
        'package': energy.pkg[socket],
        'dram': energy.dram[socket]
    } for socket in range(energy.pkg)]
    pyRAPL.Measurement('bar').begin()

    return {'rapl': {'timestamp': get_unixtime(), 'reading': energy}}


# collector utilities
PARENT_PIPE, CHILD_PIPE = Pipe()
DEFAULT_PERIOD_SECS = 0.050


def periodic_sample(sample_func, period, **kwargs):
    """ Collects data from a source periodically. """
    data = []
    while not CHILD_PIPE.poll():
        start = time()
        if 'sample_args' in kwargs:
            data.append(sample_func(kwargs['sample_args']))
        else:
            data.append(sample_func())
        sleep(max(0, DEFAULT_PERIOD_SECS - (time() - start)))
    return data


def samples_to_data_set(samples):
    """ Packs samples into a DataSet proto json. """
    data_set = {}
    for sample in samples:
        sample_type = list(sample.keys())[0]
        if sample_type not in data_set:
            data_set[sample_type] = []
        data_set[sample_type].append(sample[sample_type])
    return data_set


class EflectCollector:
    def __init__(self, pid=None, period=DEFAULT_PERIOD_SECS):
        if pid is None:
            pid = os.getpid()

        self.pid = pid
        self.period = period
        self.running = False

    def start(self):
        """ Starts data collection """
        if not self.running:
            self.running = True

            self.executor = ProcessPoolExecutor(3)
            self.data_futures = []

            # jiffies
            self.data_futures.append(self.executor.submit(
                periodic_sample,
                sample_cpu,
                self.period,
            ))
            self.data_futures.append(self.executor.submit(
                periodic_sample,
                sample_process,
                self.period,
                sample_args=self.pid,
            ))

            # energy
            self.data_futures.append(self.executor.submit(
                periodic_sample,
                sample_rapl,
                self.period,
            ))

    def stop(self):
        """ Stops data collection """
        if self.running:
            self.running = False

            PARENT_PIPE.send(1)
            self.executor.shutdown()
            CHILD_PIPE.recv()

    def read(self):
        """ Pulls samples from the futures into a DataSet proto json. """
        data = []
        for future in self.data_futures:
            try:
                data.extend(future.result())
            except:
                print('could not consume a future')
        self.data_futures = []
        return samples_to_data_set(data)


def parse_args():
    """ Parses collector arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        dest='pid',
        type=int,
        default=None,
        help='pid to collect from',
    )
    parser.add_argument(
        '-p',
        '--period',
        dest='period',
        type=float,
        default=None,
        help='sampling period for data collection',
    )
    parser.add_argument(
        '-d',
        '--duration',
        dest='duration',
        type=float,
        default=None,
        help='length of time data is collected',
    )
    parser.add_argument(
        '-o',
        '--output_dir',
        dest='output',
        default=None,
        help='directory to write the processed data to',
    )
    return parser.parse_args()


def wait_for_process(args):
    """ Wait until either the process ends, the duration ends, or ctrl-C. """
    try:
        if args.duration is None:
            try:
                while os.path.exists(os.path.join('/proc', str(args.pid))):
                    sleep(1000)
            except:
                print('process {} terminated'.format(args.pid))
        else:
            sleep(args.duration)
    except KeyboardInterrupt:
        print('user requested to end collection')


def main():
    args = parse_args()

    collector = EflectCollector(pid=args.pid, period=args.period)
    collector.start()
    wait_for_process(args)
    collector.stop()

    data = collector.read()
    if args.output is None:
        pprint(data)
    else:
        with open(args.output, 'w') as f:
            json.dump(data, f)


if __name__ == '__main__':
    main()

""" A data collector for eflect """
import os

from concurrent.futures import ProcessPoolExecutor
from multiprocessing import Pipe
from time import sleep, time

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
    data_set = {}
    for sample in samples:
        sample_type = list(sample.keys())[0]
        if sample_type not in data_set:
            data_set[sample_type] = []
        data_set[sample_type].append(sample[sample_type])
    return data_set


class EflectCollector:
    def __init__(self, pid=None, output_dir=None, period=DEFAULT_PERIOD_SECS):
        if output_dir is None:
            self.output_dir = os.getcwd()
        else:
            self.output_dir = output_dir
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
        data = []
        for future in self.data_futures:
            data.extend(future.result())
        self.data_futures = []
        return samples_to_data_set(data)


if __name__ == '__main__':
    collector = EflectCollector(pid=1)
    collector.start()
    sleep(0.1)
    collector.stop()
    print(samples_to_data_set(collector.read()))

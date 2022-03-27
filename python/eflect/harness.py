""" a client that runs a user workload monitored by an eflect server. """
import importlib
import os

from argparse import ArgumentParser

from client import EflectClient


def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        dest='file',
        nargs='*',
        default=None,
        help='name of file to be profiled',
    )
    parser.add_argument(
        '-c',
        '--code',
        dest='code',
        default=None,
        help='code to be profiled',
    )
    parser.add_argument(
        '-d',
        '--data',
        dest='data',
        default=None,
        help='path to write the data set in profiling mode; path of the data set otherwise',
    )
    parser.add_argument(
        '--addr',
        dest='addr',
        type=str,
        default='[::1]:50051',
        help='address of the eflect server',
    )
    parser.add_argument(
        '--process',
        dest='process',
        action='store_true',
        help='whether or not to process the raw data',
    )
    args = parser.parse_args()

    # check if there is a workload to run
    if len(args.file) > 0 and args.code is not None:
        raise RuntimeError('only one of --file or --code can be provided!')
    elif len(args.file) > 0:
        file = args.file[0]

        spec = importlib.util.spec_from_file_location('module.name', file)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)

        args.workload = [lambda: module.main()]
    elif args.code is not None:
        args.workload = [lambda: exec(args.code)]
    else:
        args.workload = None

    if args.workload is None:
        raise RuntimeError('one of --file or --code must be provided!')

    return args


def run_workload(workload, client):
    print('starting eflect monitoring of workload')
    client.start(os.getpid())
    workload()
    client.stop()
    print('stopped eflect monitoring of workload')


def main():
    args = parse_args()

    client = EflectClient(args.addr)

    workload = args.workload[0]
    run_workload(workload, client)
    data = client.read().data

    path = args.data if args.data is not None else 'eflect-data.pb'
    with open(path, 'wb') as f:
        f.write(data.SerializeToString())
    print('wrote data to {}'.format(path))


if __name__ == '__main__':
    main()

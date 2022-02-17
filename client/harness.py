""" a client that runs a user-defined workload monitored by an eflect server. """
import importlib
import os
import time

from argparse import ArgumentParser

import grpc

from processing import compute_footprint
from protos.sample.sample_pb2 import DataSet
from protos.sample.sampler_pb2 import ReadRequest, StartRequest, StopRequest
from protos.sample.sampler_pb2_grpc import SamplerStub

def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        # '-f',
        # '--file',
        dest='file',
        nargs='*',
        default=None,
        help='name of file to be profiled'
    )
    parser.add_argument(
        '-c',
        '--code',
        dest='code',
        default=None,
        help='code to be profiled'
    )
    parser.add_argument(
        '-d',
        '--data',
        dest='data',
        default=None,
        help='path to write the data set in profiling mode; path of the data set otherwise'
    )
    parser.add_argument(
        '--process',
        dest='process',
        action='store_true',
        help='whether or not to process the raw data'
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

    if args.workload is None and args.data is None:
        raise RuntimeError('one of --file, --code, or --data must be provided!')

    return args

def main():
    args = parse_args()

    if args.workload is not None:
        workload = args.workload[0]
        # tell eflect to monitor our runtime
        stub = SamplerStub(grpc.insecure_channel('[::1]:50051'))

        print('starting eflect monitoring of workload')
        stub.Start(StartRequest(pid=os.getpid()))
        workload()
        stub.Stop(StopRequest())
        print('stopped eflect monitoring of workload')

        data = stub.Read(ReadRequest()).data

        path = args.data if args.data is not None else 'eflect-data.pb'
        with open(path, 'wb') as f:
            f.write(data.SerializeToString())
        print('wrote data to {}'.format(path))
    else:
        # load in an existing data_set
        with open(args.data, 'rb') as f:
            data = DataSet()
            data.ParseFromString(f.read())
        print('loaded data from {}'.format(args.data))

    if args.process:
        path = os.path.join(os.path.dirname(path), 'eflect-footprint.csv')
        footprint = compute_footprint(data)
        footprint.to_csv(path)
        print('wrote footprint to {}'.format(path))

if __name__ == '__main__':
    main()

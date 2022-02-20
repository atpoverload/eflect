""" a client that can talk to an eflect sampler. """
import importlib
import os
import time

from argparse import ArgumentParser

import grpc

from protos.sample.sample_pb2 import DataSet
from protos.sample.sampler_pb2 import ReadRequest, StartRequest, StopRequest
from protos.sample.sampler_pb2_grpc import SamplerStub

def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        dest='command',
        choices=['start', 'stop', 'read'],
        help='request to make'
    )
    parser.add_argument(
        '--pid',
        dest='pid',
        type=int,
        required=True,
        help='pid to be monitored'
    )
    return parser.parse_args()

def main():
    args = parse_args()

    stub = SamplerStub(grpc.insecure_channel('[::1]:50051'))
    if args.command == 'start':
        stub.Start(StartRequest(pid=args.pid))
    elif args.command == 'stop':
        stub.Stop(StopRequest(pid=args.pid))
    elif args.command == 'read':
        print(stub.Read(ReadRequest(pid=args.pid)))

if __name__ == '__main__':
    main()

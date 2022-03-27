""" a client that can talk to an eflect sampler. """
from argparse import ArgumentParser

import grpc

from protos.sampler.sampler_pb2 import ReadRequest, StartRequest, StopRequest
from protos.sampler.sampler_pb2_grpc import SamplerStub


def parse_args():
    """ Parses client-side arguments. """
    parser = ArgumentParser()
    parser.add_argument(
        dest='command',
        choices=['start', 'stop', 'read'],
        help='request to make',
    )
    parser.add_argument(
        '--pid',
        dest='pid',
        type=int,
        default=-1,
        help='pid to be monitored',
    )
    parser.add_argument(
        '--addr',
        dest='addr',
        type=str,
        default='[::1]:50051',
        help='address of the eflect server',
    )
    return parser.parse_args()


class EflectClient:
    def __init__(self, addr):
        self.stub = SamplerStub(grpc.insecure_channel(addr))

    def start(self, pid):
        self.stub.Start(StartRequest(pid=pid))

    def stop(self):
        self.stub.Stop(StopRequest())

    def read(self):
        return self.stub.Read(ReadRequest())


def main():
    args = parse_args()

    client = EflectClient(args.addr)
    if args.command == 'start':
        if args.pid < 0:
            raise Exception(
                'the pid to monitor must be non-negative ({})'.format(args.pid))
        client.start(args.pid)
    elif args.command == 'stop':
        client.stop()
    elif args.command == 'read':
        # TODO(timur): although python3 $PWD/client.py read > eflect-data.pb is
        #   nice, it seems that the python grpc runs out of space. we should do
        #   something with "read()" to handle this
        print(client.read())


if __name__ == '__main__':
    main()

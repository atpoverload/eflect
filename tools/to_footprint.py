import os
import sys

from google.protobuf import text_format

from eflect.processing import compute_footprint
from protos.sample.sample_pb2 import DataSet

def load_data_set(data_set_path):
    """ Loads an DataSet from the path. """
    with open(data_set_path, 'rb') as f:
        data_set = DataSet()
        data_set.ParseFromString(f.read())
        return data_set

def main():
    data = load_data_set(sys.argv[1])
    footprint = compute_footprint(data)
    print(footprint[footprint > 0].dropna())

if __name__ == '__main__':
    main()

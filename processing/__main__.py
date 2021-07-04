import importlib
import os

from argparse import ArgumentParser, ArgumentError

from eflect.processing import account_energy

def parse_eflect_args():
    parser = ArgumentParser()
    parser.add_argument(
        '-o',
        '--out',
        dest='output',
        help='output file for footprints'
    )
    args = parser.parse_args()
    return args

def main():
    args = parse_eflect_args()

    if not os.path.exists(args.output):
        raise ArgumentError('specified output directory {} was not found!'.format(args.output))

    # get the footprints
    footprint, ranking = account_energy(args.output)
    footprint.to_csv(os.path.join(args.output, 'footprint.csv'))
    ranking.to_csv(os.path.join(args.output, 'ranking.csv'))

if __name__ == '__main__':
    main()

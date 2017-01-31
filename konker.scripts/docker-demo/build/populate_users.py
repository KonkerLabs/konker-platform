#! /usr/bin/env python2
import argparse
from dao.registry import create_user, users_count


def main():
    if users_count() == 0:
        parser = argparse.ArgumentParser()
        parser.add_argument('user', type=str)
        parser.add_argument('password', type=str)
        parser.add_argument('--org', type=str)
        args = parser.parse_args(["admin", "changeme"])
        create_user(args)


if __name__ == '__main__':
    main()

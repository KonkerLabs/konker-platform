#! /usr/bin/env python2
import argparse
import sys

from dao.registry import create_user, update_user, upgrade_version

usage = '''konker <command> [<args>]

The commands available are:
    user     User Management
    database Database Management
    '''


class KonkerDsl(object):
    def __init__(self):
        parser = argparse.ArgumentParser(description='Konker Platform Management', usage=usage)
        parser.add_argument('command', help='Subcommand to run')
        # parse_args defaults to [1:] for args, but you need to
        # exclude the rest of the args too, or validation will fail
        args = parser.parse_args(sys.argv[1:2])
        if not hasattr(self, args.command):
            parser.print_help()
            exit(1)
        # use dispatch pattern to invoke method with same name
        getattr(self, args.command)()

    @staticmethod
    def user():
        parser = argparse.ArgumentParser(description='User Management')
        sub_parser = parser.add_subparsers(title='subcommands', description='valid subcommands', help='Additional help')

        sub_parser_create = sub_parser.add_parser('create', description='create command', help='Create account')
        sub_parser_create.add_argument('user', help='Username', type=str)
        sub_parser_create.add_argument('password', help='Password', type=str)
        sub_parser_create.add_argument('--org', help='Organization name, account username used as default', type=str)
        sub_parser_create.set_defaults(func=create_user)

        sub_parser_update = sub_parser.add_parser('update', description='update command', help='Update account')
        sub_parser_update.add_argument('user', help='Specify the account username', type=str)
        sub_parser_update.add_argument('password', help='Specify the new account password', type=str)
        sub_parser_update.set_defaults(func=update_user)
        args = parser.parse_args(sys.argv[2:])
        args.func(args)

    @staticmethod
    def database():
        parser = argparse.ArgumentParser(description='Database management')
        sub_parser = parser.add_subparsers(title='subcommands', description='valid subcommands', help='Additional help')

        sub_parser_upgrade = sub_parser.add_parser('upgrade', description='upgrade command', help='Database upgrade')
        sub_parser_upgrade.add_argument('version', help='Database version', type=str)
        sub_parser_upgrade.set_defaults(func=upgrade_version)
        args = parser.parse_args(sys.argv[2:])
        args.func(args)


if __name__ == '__main__':
    KonkerDsl()

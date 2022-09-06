"""
This module defines a utility for printing things in an indented manner.
"""

class IndentContext:
    def __init__(self, printer):
        self._printer = printer

    def __enter__(self):
        self._printer._indent += 1

    def __exit__(self):
        self._printer._indent -= 1


class Printer:
    def __init__(self, indent=0):
        self._indent = indent

    def print(self, *args):
        thing = ''.join([str(arg) for arg in args])
        things = thing.split('\n')

        for t in things:
            print('\t' * self._indent, t)

    def indent(self):
        self._indent += 1

    def unindent(self):
        self._indent -= 1

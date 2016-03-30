import sqlite3

def on_transaction(func):
    def run(*args, **kwargs):
        self = args[0]
        with self.conn as conn:
            conn.execute(func(*args, **kwargs))
            
    return run
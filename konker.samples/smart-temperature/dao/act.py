import sqlite3
from dao.transaction import *
from config import *

class ActDao:

    SCHEMA = """
        CREATE TABLE IF NOT EXISTS act_memory (
            DEVICE_KEY TEXT PRIMARY KEY NOT NULL,
            LAST_COMMAND INTEGER NOT NULL
        );
    """

    def __init__(self):
        self.conn = sqlite3.connect(DATABASE_CONFIG['path'])
        self.init_schema()

    def init_schema(self):
        with self.conn as conn:
            conn.executescript(self.SCHEMA)

    @on_transaction
    def save_command(self,key,command):
        return """
            INSERT OR REPLACE INTO act_memory (DEVICE_KEY, LAST_COMMAND)
            VALUES ('{0}',{1})
        """.format(key,int(command))

    def get_last_command(self,key):
        sql = """
            SELECT LAST_COMMAND
            FROM act_memory
            WHERE DEVICE_KEY = '{0}'
        """

        cursor = self.conn.execute(sql.format(key))
        row = cursor.fetchone()

        if row:
            return row[0]
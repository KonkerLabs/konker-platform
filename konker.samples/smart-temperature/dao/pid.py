import sqlite3
from dao.transaction import *
from config import *

class PidDao:

    SCHEMA = """
        CREATE TABLE IF NOT EXISTS pid_params (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            DEVICE_KEY TEXT NOT NULL,
            KP REAL NOT NULL DEFAULT 0.0,
            KI REAL NOT NULL DEFAULT 0.0,
            KD REAL NOT NULL DEFAULT 0.0,
            MIN_OUT REAL NOT NULL,
            MAX_OUT REAL NOT NULL
        );

        CREATE TABLE IF NOT EXISTS pid_memory (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            ERROR REAL NOT NULL DEFAULT 0.0,
            CUMM_INTEGRAL REAL NOT NULL DEFAULT 0.0,
            SET_POINT REAL NOT NULL DEFAULT 0.0,
            CURR_TIME INTEGER NOT NULL,
            PID_PARAMS_ID INTEGER NOT NULL,
            FOREIGN KEY(PID_PARAMS_ID) REFERENCES pid_params(ID)
        );
    """

    def __init__(self):
        self.conn = sqlite3.connect(DATABASE_CONFIG['path'])
        self.init_schema()

    def init_schema(self):
        with self.conn as conn:
            conn.executescript(self.SCHEMA)

    @on_transaction
    def create_pid_entry(self,key,kp,ki,kd,min_output,max_output):
        return """
            INSERT INTO pid_params (DEVICE_KEY,KP,KI,KD,MIN_OUT,MAX_OUT)
            VALUES ('{0}',{1},{2},{3},{4},{5})
        """.format(key,kp,ki,kd,min_output,max_output)

    @on_transaction
    def update_pid_entry(self,key,kp,ki,kd,min_output,max_output):
        return """
            UPDATE pid_params SET
            KP = {1},
            KI = {2},
            KD = {3},
            MIN_OUT = {4},
            MAX_OUT = {5}
            WHERE DEVICE_KEY = '{0}'
        """.format(key,kp,ki,kd,min_output,max_output)

    @on_transaction
    def save_step(self,id,err,Ci,sp,curr_time):
        return """
            INSERT INTO pid_memory (PID_PARAMS_ID,ERROR,CUMM_INTEGRAL,SET_POINT,CURR_TIME)
            VALUES ({0},{1},{2},{3},{4})
        """.format(id,err,Ci,sp,int(curr_time))

    @on_transaction
    def reset_pid_memory_for(self,key):
        return """
            DELETE from pid_memory
            WHERE PID_PARAMS_ID in (
                SELECT ID FROM pid_params where DEVICE_KEY = '{0}'
            )
        """.format(key)

    def get_pid_entry_for(self,key):
        sql = """
            SELECT ID, KP, KI, KD, MIN_OUT, MAX_OUT
            FROM pid_params p
            WHERE p.DEVICE_KEY == '{0}'
        """

        cursor = self.conn.execute(sql.format(key))
        row = cursor.fetchone()

        if row:
            return {
                'id': row[0],
                'kp' : row[1],
                'ki': row[2],
                'kd': row[3],
                'min_out': row[4],
                'max_out': row[5]
            }

    def get_last_step_for(self,key):
        sql = """
            SELECT m.ERROR, m.CUMM_INTEGRAL, m.SET_POINT, m.CURR_TIME
            FROM pid_memory m
            JOIN pid_params p on (m.PID_PARAMS_ID == p.ID)
            WHERE p.DEVICE_KEY == '{0}'
            ORDER BY m.ID DESC
            LIMIT 1
        """
        cursor = self.conn.execute(sql.format(key))
        row = cursor.fetchone()

        if row:
            return {
                'err': row[0],
                'Ci' : row[1],
                'sp': row[2],
                'last_time' : row[3]
            }
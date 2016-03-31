import sqlite3
from dao.transaction import *
from config import *

class LookupIRCodesDao:

    SCHEMA = """
        CREATE TABLE IF NOT EXISTS EQUIPMENTS (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            BRAND           TEXT    NOT NULL,
            MODEL           TEXT    NOT NULL
        );

        CREATE TABLE IF NOT EXISTS CODES (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            EQUIPMENT_ID    INT     NOT NULL,
            COMMAND         TEXT    NOT NULL,
            CODE            TEXT    NOT NULL,
            FOREIGN KEY(EQUIPMENT_ID) REFERENCES EQUIPMENTS(ID)
        );
    """

    def __init__(self):
        self.conn = sqlite3.connect(DATABASE_CONFIG['path'])
        self.init_schema()

    def init_schema(self):
        with self.conn as conn:
            conn.executescript(self.SCHEMA)

    def get_ir_code_for(self,brand,model,command):
        sql = """
            SELECT c.CODE
            FROM CODES c
            JOIN EQUIPMENTS e on (c.EQUIPMENT_ID == e.ID)
            WHERE e.BRAND == '{0}'
              AND e.MODEL == '{1}'
              AND c.COMMAND == '{2}'
        """

        cursor = self.conn.execute(sql.format(brand,model,command))
        row = cursor.fetchone()

        if row:
            return {
                'code': row[0]
            }
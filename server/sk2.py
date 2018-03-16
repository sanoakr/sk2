#!/usr/bin/env /usr/bin/python3.6

# attendance server 20171128
#
# self-signed certificates: openssl req -new -x509 -days 1000 -nodes -out cert.pem -keyout cert.pem
#

import asyncio
import ssl
import os
import re
import hashlib
import ldap3
import MySQLdb as db
from logging import getLogger, FileHandler, DEBUG, Formatter

server_ip = "133.83.30.65"
server_port = 4440

db_args = {"user": "sk2",
           "passwd": "sk2loglog",
           "host": "localhost",
           "db": "sk2"
}
db_tbl = "test"

sk2_dir = "/usr/local/sk2/"
data_dir = sk2_dir + "data/"
salt_dir = sk2_dir + "key/"
log = r"/usr/local/sk2/log/sk2.log"

# EDS
eds = "setaeds.seta.ryukoku.ac.jp"
eds_sbase = "o=Ryukoku,c=JP" 
Certfile = "/etc/letsencrypt/live/sk2.st.ryukoku.ac.jp/fullchain.pem"
Keyfile = "/etc/letsencrypt/live/sk2.st.ryukoku.ac.jp/privkey.pem"

# date encoding salt
Saltfile = salt_dir + "user.salt"
# replying info key salt
replySaltfile = salt_dir + "reply.salt"

# for test
testuser = "testuser"


class AsyncClient(asyncio.Protocol):
    def connection_made(self, transport):
        peername = transport.get_extra_info('peername')
        logger.info('Connectted from {}'.format(peername)) 
        self.transport = transport

    def data_received(self, data):
        message = data.decode() # to data string
        logger.info('Received: {!r}...'.format(message[0:15]))
        pieces = message.split(',')

        # Authentication
        if len(pieces) == 3 and pieces[0] == "AUTH":
            user = pieces[1]
            pwd = pieces[2]
            if (user == testuser):
                auth = (testuser, "テストユーザ")
            else:
                auth = self.ldapAuth(user, pwd)

            if auth:
                (gcos, name) = auth # md5 encode with reply.salt
                reply_key = hashlib.md5((user + reply_salt).encode()).hexdigest() # reply info key for getting history
                reply_msg = "{},{},{}".format(reply_key, gcos, name)
                logger.info("AUTH success {} ".format(user))
            else:
                reply_msg = "authfail"
                logger.info("AUTH fail {} ".format(user))

        # sk2 Log write
        elif len(pieces) >= 6: # 6 = user(1)+marker(1)+datetime(1)+APdata1(3)
            user = pieces[0]

            # md5 encode username(uid) with salt
            md5_uid = hashlib.md5((user + salt).encode()).hexdigest()
            filename = data_dir + md5_uid
            md5_message = re.sub(r'^'+user, md5_uid, message)

            # to textfile
            try:
                with open(filename, mode='ab') as out:
                    out.write(md5_message.encode())
                    out.write(b'\n')
                    reply_msg = "success"
                    logger.info("LOG write success {} ".format(user))
            except:
                reply_msg = "fail"
                logger.info("LOG write fail {} ".format(user))

            # convert NULL data to 0
            for i in range(18):
                if pieces[i] == '': 
                    pieces[i] = None
                elif i >= len(pieces):
                    pieces.append(None)

            # to database
            with db.connect(**db_args) as cur:
                cur.execute("INSERT INTO " + db_tbl
                            + " (id, type,"
                            + " datetime,"
                            + " ssid0, bssid0, signal0,"
                            + " ssid1, bssid1, signal1,"
                            + " ssid2, bssid2, signal2,"
                            + " ssid3, bssid3, signal3,"
                            + " ssid4, bssid4, signal4)"
                            + " VALUES (%s, %s, %s,"
                            + " %s, %s, %s,"
                            + " %s, %s, %s,"
                            + " %s, %s, %s,"
                            + " %s, %s, %s,"
                            + " %s, %s, %s)",
                            (md5_uid, pieces[1], pieces[2],
                             pieces[3], pieces[4], int(pieces[5] or 0),
                             pieces[6], pieces[7], int(pieces[8] or 0),
                             pieces[9], pieces[10], int(pieces[11] or 0),
                             pieces[12], pieces[13], int(pieces[14] or 0),
                             pieces[15], pieces[16], int(pieces[17] or 0)))

        # Fail
        else:
            reply_msg = "fail"
            logger.info("LOG any fail {} ".format(user))

        self.transport.write(reply_msg.encode()) # data bytes
        logger.info('Sent: {!r}'.format(reply_msg))

        self.transport.close()
        logger.info('Close the client socket')
        return

    def ldapAuth(self, id, ps):
        try:
            s = ldap3.Server(eds, use_ssl=True)
            c = ldap3.Connection(s, auto_bind=True) 
            c.search(search_base=eds_sbase, search_filter="(uid={})".format(id))
            dn = c.response[0]['dn']
        except:
            logger.info("ldap search error {}".format(id))
            return False

        try:
            c = ldap3.Connection(s, auto_bind=True, user=dn, password=ps)
            c.search(search_base=eds_sbase, search_filter="(uid={})".format(id), attributes=['gecos', 'displayName'])
            gecos = c.response[0]['raw_attributes']['gecos'][0].decode("utf-8")
            name =  c.response[0]['raw_attributes']['displayName'][0].decode("utf-8")
            return (gecos, name)
        except:
            return False


if __name__ == '__main__':
    # Logger
    logger = getLogger(__name__)
    if not logger.handlers:
        fileHandler = FileHandler(log)
        fileHandler.setLevel(DEBUG)
        formatter = Formatter('%(asctime)s:%(message)s')
        fileHandler.setFormatter(formatter)
        logger.setLevel(DEBUG) 
        logger.addHandler(fileHandler)

    logger.info('logfile: {}'.format(log))
    logger.info('data directorye: {}'.format(data_dir))

    # md5 data salt
    with open(Saltfile) as sf:
        salt = sf.readline()
        logger.info('read user salt: {}'.format(Saltfile))
    # md5 reply salt
    with open(replySaltfile) as rf:
        reply_salt = rf.readline()
        logger.info('read reply salt: {}'.format(replySaltfile))

    # make asyncio loop
    loop = asyncio.get_event_loop()
    # with ssl
    context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
    context.load_cert_chain(certfile=Certfile, keyfile=Keyfile)
    coro = loop.create_server(AsyncClient, host=server_ip, port=server_port, ssl=context)

    #coro = loop.create_server(AsyncClient, server_ip, server_port)
    server = loop.run_until_complete(coro)
    logger.info('start sk2 attend: {}'.format(server.sockets[0].getsockname()))

    # Serve requests until Ctrl+C is pressed
    try:
        loop.run_forever()
    except KeyboardInterrupt:
        pass

    # Close the server
    server.close()
    loop.run_until_complete(server.wait_closed())
    loop.close()
    logger.info('stop sk2 atternd.')


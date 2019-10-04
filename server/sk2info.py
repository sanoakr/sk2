#!/usr/bin/env /usr/bin/python3.6

# attendance server 20171128
#
# self-signed certificates:
# openssl req -new -x509 -days 1000 -nodes -out cert.pem -keyout cert.pem

import asyncio
import ssl
import os
import re
import hashlib
from logging import getLogger, FileHandler, DEBUG, Formatter

server_ip = "133.83.30.65"
server_port = 4441

sk2_dir = "/usr/local/sk2/"
data_dir = sk2_dir + "data/"
salt_dir = sk2_dir + "key/"
log = r"/usr/local/sk2/log/sk2info.log"

Certfile = "/etc/letsencrypt/live/sk2.st.ryukoku.ac.jp/fullchain.pem"
Keyfile = "/etc/letsencrypt/live/sk2.st.ryukoku.ac.jp/privkey.pem"

# date encoding salt
Saltfile = salt_dir + "user.salt"
# replying info key salt
replySaltfile = salt_dir + "reply.salt"

max_reverslines = 100

#mac = re.compile("[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}:[[[[[0-9a-f]{2}")
#room_file = sk2_dir + "room.csv"

class AsyncClient(asyncio.Protocol):
    def connection_made(self, transport):
        peername = transport.get_extra_info('peername')
        logger.info('Connectted from {}'.format(peername))
        self.transport = transport

    def data_received(self, data):
        message = data.decode()  # to data string
        logger.info('Received: {!r}...'.format(message[0:15]))
        pieces = message.split(',')

        # history info
        if len(pieces) == 2:
            user = pieces[0].strip().lower() # ユーザー名は前後空白を削除して小文字に
            #user = pieces[0]
            key = pieces[1]

            correct_key = hashlib.md5((user + reply_salt).encode()).hexdigest()
            if key == correct_key:
                logger.info("Key success {} ".format(user))
                md5_uid = hashlib.md5((user + salt).encode()).hexdigest()
                filename = data_dir + md5_uid

                s_message = ''
                try:
                    with open(filename) as fp:
                        for line in readlines_reverse(fp, max_reverslines):
                            s_message += line + '\n'
                    rflag = True
                except:
                    rflag = False

                if rflag:
                    s_message = s_message.rstrip('\n')
                    #mac_list = mac.findall(s_message)
                    # for m in mac_list:
                    #    if m in room_map:
                    #        s_message = s_message.replace(m, room_map[m])
                    logger.info("Get history {} ".format(user))
                else:
                    s_message = "Error"
                    logger.info("Some Error {} ".format(user))
            else:
                s_message = "Wrong key."
                logger.info("Key fail {} ".format(user))
        else:
            s_message = "Wrong format"
            logger.info("Wrong format {} ".format(user))

        self.transport.write(s_message.encode())  # data bytes
        logger.info('Sent: {!r}...'.format(s_message[0:40]))

        self.transport.close()
        logger.info('Close the client socket')
        return

# make dictionary : bssid -> room
# def room_table(filename):
#     try:
#         with open(filename) as f:
#             room = {}
#             for line in f:
#                 data = line.split(',')
#                 room[data[9].strip()] = "{0}{1}{2} {3}".format(data[0],data[1],data[2],data[4])
#     except:
#         return False
#     return room

# read lines from EOF


def readlines_reverse(fp, read_max):
    position = fp.seek(0, os.SEEK_END) - 2  # eliminate last newline
    line = ''
    count = 0
    while position >= 0 and count < read_max:
        fp.seek(position)
        next_char = fp.read(1)
        if next_char == "\n":
            yield line[::-1]
            count += 1
            line = ''
        else:
            line += next_char
        position -= 1
    yield line[::-1]


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

    # make BSSID to ROOM table
    # room_map = room_table(room_file)
    # if room_map:
    #     logger.info("read BSSID-Room table: {}".format(room_file))
    # else:
    #     logger.info("Error: can't read BSSID-Room table: {}".format(room_file))
    #     exit()

    # make asyncio loop
    loop = asyncio.get_event_loop()
    # with ssl
    context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
    context.load_cert_chain(certfile=Certfile, keyfile=Keyfile)
    coro = loop.create_server(AsyncClient,
                              host=server_ip,
                              port=server_port,
                              ssl=context)
    #coro = loop.create_server(AsyncClient, server_ip, server_port)
    server = loop.run_until_complete(coro)
    logger.info('start sk2 info: {}'.format(server.sockets[0].getsockname()))

    # Serve requests until Ctrl+C is pressed
    try:
        loop.run_forever()
    except KeyboardInterrupt:
        pass

    # Close the server
    server.close()
    loop.run_until_complete(server.wait_closed())
    loop.close()
    logger.info('stop sk2 info.')

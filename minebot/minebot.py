import discord
from discord.ext import commands
from dotenv import load_dotenv
from threading import Thread
from time import sleep
from os import getenv
from sys import exit
import asyncio
import socket

load_dotenv()
TOKEN = getenv('TOKEN')
DEBUG = getenv('DEBUG')
channel_id = int(getenv('CHANNEL_ID'))
commandChannel_id = int(getenv('COMMAND_CHANNEL_ID'))
threads = []
conns = []

gameQueue = asyncio.Queue(maxsize=16777216)
discordQueue = asyncio.Queue(maxsize=16777216)
client = commands.Bot(command_prefix='>', intents=discord.Intents.default()) #client = discord.Client()

# Discord bot stuff
@client.event
async def on_ready():
  print(f'{client.user} has connected to Discord!')

@client.event
async def on_message(message):
  if message.author == client.user:
    return
  await client.process_commands(message)
  if message.channel.id == channel_id:
    content = f'{message.author}: {message.content}'
    await discordQueue.put(content)

# Sender for discord chat to plugin
async def sendFromDiscordQueue():
  print('sendFromDiscordQueue running!')
  while 'chatting':
    message = await discordQueue.get()
    print(f'Sending message "{message}" to plugin.')
    for conn in conns:
      conn.send(f'{message}\n'.encode('utf-8'))

# Sender for game chat to discord
async def sendFromGameQueue():
  print('sendFromGameQueue running!')
  await client.wait_until_ready()
  channel = await client.fetch_channel(channel_id)
  commandChannel = await client.fetch_channel(commandChannel_id)
  while not client.is_closed():
    await asyncio.sleep(1)
    message = await gameQueue.get()
    print(f'Sending message "{message}" to discord.')
    if message.split(' ')[1][0] == '/': await commandChannel.send(message)
    else: await channel.send(message)

# Listener for minecraft chat sent by plugin
async def handleClient(conn):
  print('started minecraft plugin listener!')
  while "we're coocking with gas":
    message = conn.recv(4096).decode('utf-8').strip()
    if message:
      await gameQueue.put(message)

def listener(sock):
  print('starting listener.')
  sock.listen(7)
  while 'eavesdropping':
    conn, addr = sock.accept()
    newThread = Thread(
      target=asyncio.run, args=(handleClient(conn),)
    )
    newThread.start()
    threads.append(newThread)
    conns.append(conn)

# If main do stuff
if __name__ == '__main__':
  try:
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.bind(('0.0.0.0', 1337))
    print('starting server.')
    threads.append(Thread(target=listener, args=(sock,)))
    asyncio.set_event_loop(client.loop)
    client.loop.create_task(sendFromDiscordQueue())
    client.loop.create_task(sendFromGameQueue())
    for thread in threads:
      thread.start()
    print('Client ready!')
    client.run(TOKEN)
  except OSError:
    print('Address in use!')
    exit(1)
  except KeyboardInterrupt:
    print("Got keyboard interrupt.")
    exit(1)
  except Exception as err:
    print(f'Got unexpected error!\n{dir(err)}\n{err.message}')
  finally:
    sock.close()
    for thread in threads:
      thread.join()
    client.loop.run_until_complete(client.logout())
    for t in asyncio.Task.all_tasks(loop=client.loop):
      if t.done():
        t.exception()
        continue
      t.cancel()
      try:
        client.loop.run_until_complete(asyncio.wait_for(t, 5, loop=client.loop))
        t.exception()
      except asyncio.InvalidStateError:
        pass
      except asyncio.TimeoutError:
        pass
      except asyncio.CancelledError:
        pass
    client.loop.close()

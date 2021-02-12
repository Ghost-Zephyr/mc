FROM python:3.9-buster

COPY ./requirements.txt /opt
WORKDIR /opt
RUN pip3 install -r requirements.txt

CMD [ "python3", "minebot.py" ]

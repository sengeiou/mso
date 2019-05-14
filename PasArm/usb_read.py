import serial
import re
import os


ser = serial.Serial()
ser.port = 'COM5'
ser.baudrate = '115200'
ser.open()

f = open("output.txt", 'w')
f.close()
os.remove("output.txt")

if(ser.isOpen()):
    try:
        #f = open("output.txt", 'a')
        while(1):
            out = ser.readline().decode('utf-8')[:-1]
            if out:
                print(out)
                f = open("output.txt", 'a')
                f.write(out)
                f.close()

    except Exception:
        print("error")
else:
    print("Cannot open serial port")


#!/bin/sh
sleep 2
route="/home/pi/mjpg-streamer/mjpg-streamer-code-182/mjpg-streamer"
cd  $route
sudo $route/mjpg_streamer -i "$route/input_uvc.so -d /dev/video0 -n -f 7 -r QVGA" -o "$route/output_http.so -n -w $route/www"

exit 0


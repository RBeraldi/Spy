#Credits: https://gist.github.com/mdonkers/63e115cc0c79b4f6b8b3a6b797e485c7
from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler
import json as js
import sys

class RequestHandler(BaseHTTPRequestHandler):
  def _set_headers(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()

  def do_GET(self):
    f=open('readings.txt','r')
    self.send_response(200)
    self.end_headers()
    self.wfile.write(f.read())
    f.close()

  def do_POST(self):
     content_length = int(self.headers['Content-Length']) # <--- Gets the size of data
     post_data=self.rfile.read(content_length)
     f=open('readings.txt','a')
     data= js.loads(post_data.decode('utf-8'))
     #f.write(data)
     #f.write(data["time"]+" "+data["azimut"]+"\n")
     f.write(post_data.decode('utf-8')+"\n")
     #f.write(data)
     f.close()
     self._set_headers()
     self.wfile.write("{\"0\":\"0\"}")

if __name__ == "__main__":
   ip=sys.argv[1]
   port=8000
   server = HTTPServer((ip, port), RequestHandler)
   print 'Server started on ip',ip,'port',port,'waiting....'
   server.serve_forever();

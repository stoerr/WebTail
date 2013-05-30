WebTail
=======

This is a simplest possible implementation of tail -F for a file on the web : 
every 5 seconds it checks whether the content provided in an URL has grown in size
and, if so, retrieves and prints the difference. Usable e.g. for log files available
through an apache.

Usage:
java -jar WebTail.jar url proxyhost proxyport
The proxy arguments are optional.

Status:
This is only a barebone implementation which I did since
http://www.jibble.org/webtail/ failed me for some unknown reason.
Feel free to extend. :-)
GPL licence.

Hans-Peter Stoerr
http://www.stoerr.net/

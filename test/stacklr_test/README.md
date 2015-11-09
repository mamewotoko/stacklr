stacklr UI test
===============

How to start
------------
```bash
ant debug install clear test
```

MEMO
----
* target application (stacklr) should have write permission to take screen shot
  i.e. add following line to stacklr/AndroidManifest.xml
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
* ant task "clear" is added to clear application data before testing

Reference
---------
* Robotium JavaDoc
  http://robotium.googlecode.com/svn/doc/index.html

stacklr - Stack based shopping/TODO list
========================================
Concept
-------
### UI
<pre>
  +----------
  | Apple   ^
  | Orange  | items to buy 
  =========== border
  | Banana  | history 
  | Sugar   V
  +---------
</pre>
### add to stack
<pre>
  +---------+
  | Apple   |
  | Orange  |
  ===========
  | Banana  | <- touch
  | Sugar   |
  +---------+

  +---------+
  | Banana  | <- push 
  | Apple   |
  | Orange  |
  ===========
  | Sugar   |
  +---------+
</pre>

### pop from stack
<pre>
  +---------+
  | Apple   |
  | Orange  | <- touch
  ===========
  | Banana  | 
  | Sugar   |
  +---------+

  +---------+
  | Apple   |
  ===========
  | Banana  | 
  | Sugar   |
  | Orange  | <- push? 
  +---------+
</pre>

Functions
---------
- pop
- push
- enter new item to list
   from app
   from pc
- add timestamp to each list item
- undo?
- search item?
- recommend new item?
- customize push order
    simple list
    sort by frequency of touch
    ...
- support due date

TODO
-----
Design
  Web UI?
  Android Native App?
  Sync with google calendar/spreadsheet?


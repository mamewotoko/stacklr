stacklr - Stack based shopping/TODO list
========================================
Concept
-------
### UI

  +----------
  | Apple   ^
  | Orange  | items to buy 
  =========== border
  | Banana  | history 
  | Sugar   V
  +---------

### add to stack

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

### pop from stack

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


TODO
-----
Design
  Web UI?
  Android Native App?
  Sync with google calendar/spreadsheet?


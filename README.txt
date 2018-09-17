An OrderBoard implementation that supports register, cancel and summary operation.

Note that, the main logic of the implementation resides in com.cs.sbm.impl.OrderBoardImpl class.

-----------------------------------
Key Points of this implementation
-----------------------------------

1. Orders are stored in a big array which is initialised at the time of start up. This helps to allocate contiguous memory at the very start and also helps towards performance. 
2. Price points are maintained in a ConcurrentSkipList map, which provides O(log n) performance for look up to locate a price point.
3. Cancel operation only removes the order from the price level chain and orders never removed from the storage array. This once again helps towards GC. Also avoids array copying which will be necessary otherwise.
4. A price level (or price point) is conceptualised in com.cs.sbm.impl.PriceLevel class.
   This class does not hold Orders in a list or array, instead each order will point to the previous and next order in the price level, thus forming a chain without a linked list.
   When an order is added or removed, appropriate previous and next pointers are updated.
   Any update/remove operation on PriceLevel is protected by a StampedLock to avoid concurrent modification of price level. Lock is obtained at a price level, in order to avoid locking entire order book.
  
 ======================
 
 There is also a Console based UI provided as com.cs.sbm.OrderBoardUI class. 
 Note that, this class has been provided only as an aid to interact, as such does not validate the input data.
 
 There is also a unit test provided, but it is not written to cover all the possible scenarios.


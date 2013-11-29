#include <stdio.h>
#include <stdlib.h>
#include <string.h>
// The Gimple Garbage Collector.


//===============================================================//
// The Java Heap data structure.

/*   
      ----------------------------------------------------
      |                        |                         |
      ----------------------------------------------------
      ^\                      /^
      | \<~~~~~~~ size ~~~~~>/ |
    from                       to
 */
struct JavaHeap
{
  int size;         // in bytes, note that this if for semi-heap size
  char *from;       // the "from" space pointer
  char *fromFree;   // the next "free" space in the from space
  char *to;         // the "to" space pointer
  char *toStart;    // "start" address in the "to" space
  char *toNext;     // "next" free space pointer in the to space
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.
void Tiger_heap_init (int heapSize)
{
  // You should write 7 statement here:
  // #1: allocate a chunk of memory of size "heapSize" using "malloc"
	char * ptr = (char *)malloc(heapSize);
	if(ptr == NULL){
		fprintf(stderr, "ERROR: malloc() failed!\n");
		exit(1);
	}

  // #2: initialize the "size" field, note that "size" field
  // is for semi-heap, but "heapSize" is for the whole heap.
	heap.size = heapSize / 2;

  // #3: initialize the "from" field (with what value?)
	heap.from = ptr;

  // #4: initialize the "fromFree" field (with what value?)
	heap.from = ptr;

  // #5: initialize the "to" field (with what value?)
	heap.to = ptr + heap.size;

  // #6: initizlize the "toStart" field with NULL;
	heap.toStart = NULL;

  // #7: initialize the "toNext" field with NULL;
	heap.toNext = NULL;

  return;
}

// The "prev" pointer, pointing to the top frame on the GC stack. 
// (see part A of Lab 4)
void *prev = 0;



//===============================================================//
// Object Model And allocation


// Lab 4: exercise 11:
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    ----------------
      | vptr      ---|----> (points to the virtual method table)
      |--------------|
      | isObjOrArray | (0: for normal objects)
      |--------------|
      | length       | (this field should be empty for normal objects)
      |--------------|
      | forwarding   | 
      |--------------|\
p---->| v_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | v_{size-1}   | /e
      ----------------/
*/
// Try to allocate an object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new (void *vtable, int size)
{
  // // You should write 4 statements for this function.
  // // #1: "malloc" a chunk of memory of size "size":
  // void * obj = malloc(size);
  // if(obj < 0){
  //   fprintf(stderr, "malloc failed!");
  //   exit(1);
  // }
  // // #2: clear this chunk of memory (zero off it):
  // memset(obj, 0, size);
  // // #3: set up the "vtable" pointer properly:
  // *((int **)obj) = (int *)vtable;  /* YKG. Consider the vptr is the first field of the struct. */
  // // #4: return the pointer
  // return obj;

   // You should write 4 statements for this function.
   // #1: "malloc" a chunk of memory of size "size":
   void * obj = malloc(size);
   if(obj < 0){
	   fprintf(stderr, "ERROR: malloc() failed!\n");
	   exit(1);
   }
   // #2: clear this chunk of memory (zero off it):
   memset(obj, 0, size);

   // #3: set up the "vtable" pointer properly:
   *((int **)obj) = (int *)vtable;  /* YKG. Consider the vptr is the first field of the struct. */

   // #3.5 other fields
   *((int *)obj + 1) = 0;		/* isObjOrArray = 0; */

   // #4: return the pointer
   return obj;
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length and other information.
/*    ----------------
      | vptr         | (this field should be empty for an array)
      |--------------|
      | isObjOrArray | (1: for array)
      |--------------|
      | length       |
      |--------------|
      | forwarding   | 
      |--------------|\
p---->| e_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | e_{length-1} | /e
      ----------------/
*/
// Try to allocate an array object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this array object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new_array (int length)
{
  // // You can use the C "malloc" facilities, as above.
  // // Your code here:
  // void * arr = malloc((length + 1) * sizeof(int));
  // if(arr < 0){
  //   fprintf(stderr, "malloc failed!");
  //   exit(1);
  // }
  // // #2: clear this chunk of memory (zero off it):
  // memset(arr, 0, (length + 1) * sizeof(int));
  // *((int *)arr) = length;
  // return (void *)(((int *)arr) + 1);

   // You can use the C "malloc" facilities, as above.
   // Your code here:
   int * arr = (int *)malloc((length + 4) * sizeof(int));
   if(arr < 0){
	   fprintf(stderr, "ERROR: malloc() failed!\n");
	   exit(1);
   }
   // #2: clear this chunk of memory (zero off it):
   memset(arr, 0, (length + 4) * sizeof(int));

   arr[0] = 0;	/* vptr = NULL; */
   arr[1] = 1;	/* isObjOrArray = 1 */
   arr[2] = length;
   arr[3] = 0;  /* forwarding = NULL; */

   return (void *)arr;
}

//===============================================================//
// The Gimple Garbage Collector

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
static void Tiger_gc ()
{
  // Your code here:
  
}


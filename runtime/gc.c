#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

static void Tiger_gc ();
void dump_Obj_Header(int addr);

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
  heap.fromFree = ptr;

  // #5: initialize the "to" field (with what value?)
  heap.to = ptr + heap.size;

  // #6: initizlize the "toStart" field with NULL;
  heap.toStart = NULL;

  // #7: initialize the "toNext" field with NULL;
  heap.toNext = NULL;

  return;
}

void dump_heap()
{
  //fprintf(stderr, "=====================================\n");
  //fprintf(stderr, "%8x heap.size \n", heap.size);
  //fprintf(stderr, "%8x heap.from \n", heap.from);
  //fprintf(stderr, "%8x heap.fromFree \n", heap.fromFree);
  //fprintf(stderr, "%8x heap.to \n", heap.to);
  //fprintf(stderr, "%8x heap.toStart \n", heap.toStart);
  //fprintf(stderr, "%8x heap.toNext \n", heap.toNext);
  //fprintf(stderr, "%8x %d::: heap.usedFrom \n", heap.fromFree - heap.from, heap.fromFree - heap.from);
  //fprintf(stderr, "%8x ::: heap.usedTo \n", heap.toNext - heap.to);

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
  //   //fprintf(stderr, "malloc failed!");
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
   //void * obj = malloc(size);
  //fprintf(stderr, "--------------------------------------@@@@@@@@@@ Tiger_new %x  %d>>>>>\n", size, size);

  if(heap.fromFree - heap.from + size > heap.size){
    Tiger_gc();
    if(heap.fromFree - heap.from + size > heap.size){
      fprintf(stderr, "ERROR: OutOfMemory!\n");
      exit(2);
    }
  }
  char * obj = heap.fromFree;
  heap.fromFree += size;

   if(obj < 0){
     fprintf(stderr, "ERROR: malloc() failed!\n");
     exit(1);
   }
   // #2: clear this chunk of memory (zero off it):
   memset(obj, 0, size);

   // #3: set up the "vtable" pointer properly:
   *((int **)obj) = (int *)vtable;  /* YKG. Consider the vptr is the first field of the struct. */

   // #3.5 other fields
   *((int *)obj + 1) = 0;   /* isObjOrArray = 0; */
   *((int *)obj + 3) = (int)obj;    /* forwarding = obj; */

   //fprintf(stderr, "new obj:\n");
  dump_Obj_Header((int)obj);
  dump_heap();
  //fprintf(stderr, "=====>>>> Tiger_new ret: %x>>>>>\n", obj);
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
  //   //fprintf(stderr, "malloc failed!");
  //   exit(1);
  // }
  // // #2: clear this chunk of memory (zero off it):
  // memset(arr, 0, (length + 1) * sizeof(int));
  // *((int *)arr) = length;
  // return (void *)(((int *)arr) + 1);

   // You can use the C "malloc" facilities, as above.
   // Your code here:
  int size = (length + 4) * sizeof(int);

  //fprintf(stderr, "--------------------------------------@@@@@@@@@@ Tiger_new_array %x  %d>>>>>\n", size, size);

  if(heap.fromFree - heap.from + size > heap.size){
    Tiger_gc();
    if(heap.fromFree - heap.from + size > heap.size){
      fprintf(stderr, "ERROR: OutOfMemory!\n");
      exit(2);
    }else{
      //fprintf(stderr, "OK!!! resume after GC.\n");
    }
  }

//   int * arr = (int *)malloc(size);
  int * arr = (int *)heap.fromFree;
  heap.fromFree += size;

   // #2: clear this chunk of memory (zero off it):
   memset(arr, 0, size);

   arr[0] = 0;  /* vptr = NULL; */
   arr[1] = 1;  /* isObjOrArray = 1 */
   arr[2] = length;
   arr[3] = (int)arr; /* forwarding = obj; */

  dump_Obj_Header((int)arr);
  dump_heap();
  //fprintf(stderr, "=====>>>> Tiger_new_array ret: %x>>>>>\n", arr);

   return (void *)arr;
}

//===============================================================//
// The Gimple Garbage Collector


static void swap(void *a, void *b)
{
  int aa, bb;

  aa = *((int *)a);
  bb = *((int *)b);

  *((int *)a) = bb;
  *((int *)b) = aa;  
}

static int IsObjInitialized(int obj_Addr)
{
  //fprintf(stderr, "------------------------@@@@@@ IsObjInitialized   %x\n", obj_Addr);
  if (obj_Addr)
  {
    // printf("Inited\n");
    // assert((int)heap.from <= obj_Addr && obj_Addr <= (int)heap.to + heap.size);  
    if (heap.from < heap.to)
    {
      assert((int)heap.from <= obj_Addr && obj_Addr <= (int)heap.to + heap.size);  
    }else{
      assert((int)heap.to <= obj_Addr && obj_Addr <= (int)heap.from + heap.size);  
    }
  }
  
  return obj_Addr != 0;
}

int GetForwarding(int obj_Addr)
{
  return *((int *)obj_Addr + 3);    
}

int GetObjType(int obj_Addr)
{
  return *((int *)obj_Addr + 1);  
}

int GetFieldCount(int obj_Addr)
{
  void * vptr = (void *)((int *)obj_Addr)[0];
  char * gc_map = *((char **)vptr);    
  return strlen(gc_map);
}


int GetObjSize(int obj_Addr)
{
  if(GetObjType(obj_Addr) == 0){
    return (4 + GetFieldCount(obj_Addr))*sizeof(int);
  }else{
    int length = *((int *)obj_Addr + 2);
    return (4 + length)*sizeof(int);
  }  
}



static int IsInToSpace(int obj_Addr)
{
    static int count = 0;
    //fprintf(stderr, "--------------------------------------@@@@@@@@@@ IsInToSpace   %x\n", obj_Addr);

    if ((int)heap.to <= (int)obj_Addr &&
        (int)obj_Addr < (int)heap.toNext){ /* already in 'to' */
      //fprintf(stderr, "HIT already in to NO.%d : %x\n", count++, obj_Addr);
      return 1;
    }else{      
      if (GetForwarding(obj_Addr) != obj_Addr)
      {
        //fprintf(stderr, "HIT already moved to TO.\n");        
        return 2;
      }else{
        //fprintf(stderr, "LOSS %d: %x\n", count++, obj_Addr);
        return 0;  
      }      
    }    
}


void dump_GC_frame(int addr)
{
    int i = 0;
    int * p = (int *)addr;
    //fprintf(stderr, "--------------------------------------@@@@@@@@@@ dump_GC_frame   %x>>>>>\n", addr);
    for(i = 0; i < 4; i++){
      //fprintf(stderr, ">>>frame: %x\n", p[i]);
    }
    //fprintf(stderr, "=================<<<<<\n");

    int prev = addr;
    void * gc_prev = *((void **)prev);
    char * arguments_gc_map = (char *)*((void **)prev + 1);
    int * arguments_base_address = (int *)*((void **)prev + 2);
    // char * locals_gc_map = (char *)*((void **)prev + 3);
    int localRefCount = *((int *)prev + 3);
    //fprintf(stderr, ">>>frame: %8x gc_prev\n", gc_prev);
    //fprintf(stderr, ">>>frame: %8x arguments_gc_map\n", arguments_gc_map);
    //fprintf(stderr, ">>>frame: %8x arguments_base_address\n", arguments_base_address);
    //fprintf(stderr, ">>>frame: %8x localRefCount\n", localRefCount);


    // int i = 0;
    for (i = 0; i < localRefCount; ++i)
    {
      //fprintf(stderr, ">>>frame: %8x refs\n", *((int *)prev + 4 + i));
    }    
}

void dump_Obj_Header(int addr)
{
    int i = 0;
    int * p = (int *)addr;
    for(i = 0; i < 4; i++){
      //fprintf(stderr, ">>>obj: %x\n", p[i]);
    }
    //fprintf(stderr, "=================<<<<<\n");

    int * obj = p;
    void * vptr = (void *)obj[0];    
    int isObjOrArray = obj[1];
    int length = obj[2];
    char ** from_forwarding_Addr = (char **)&obj[3];  /* &obj.forwarding in from space */

    //fprintf(stderr, ">>>obj: %8x vptr\n", vptr);
    //fprintf(stderr, ">>>obj: %8x isObjOrArray\n", isObjOrArray);
    //fprintf(stderr, ">>>obj: %8x length\n", length);
    //fprintf(stderr, ">>>obj: %8x forwarding  &:%8x\n", *from_forwarding_Addr, from_forwarding_Addr);
    
}






static int CopyObject(int obj_Addr)
{
  static int count = 0;
  //fprintf(stderr, "--------------------------------------@@@@@@@@@@ CopyObject %x  count:%d>>>>>\n", obj_Addr, count++);

  // //fprintf(stderr, "::::CopyObject::::  %d\n", count++);
 //  //fprintf(stderr, "vvvv: %x\n", *((int *)obj_Addr));
    int * obj = (int *)obj_Addr;
    // //fprintf(stderr, "I am here.CCCCopy111122223333444555\n");
    //fprintf(stderr, "addr: %x addr: %x  vvvv: %x\n", (int)obj, obj_Addr, *((int *)obj_Addr));
    dump_Obj_Header(obj_Addr);


    void * vptr = (void *)obj[0];
    // //fprintf(stderr, "I am here.CCCCopy111122223333444\n");
    int isObjOrArray = obj[1];
    // //fprintf(stderr, "I am here.CCCCopy111122223333\n");
    int length = obj[2];
    char ** from_forwarding_Addr = (char **)&obj[3];  /* &obj.forwarding in from space */

    int objSize;

    // //fprintf(stderr, "I am here.CCCCopy1111++++++++++++\n");
    if (isObjOrArray == 0){  /* YKG. normal object */ 
    //fprintf(stderr, "I am here.CCCCopy**************========= vptr: %x\n", vptr);       
    //fprintf(stderr, "I am here.CCCCopy**************========= %s\n", *(char **)vptr);       
      int fieldCount = strlen(*(char **)vptr); /* strlen(field_gc_map) */
      objSize = (4 + fieldCount)*sizeof(int); /* 4 for obj header */
    }else{
    //fprintf(stderr, "I am here.CCCCopy**************========= %s %d\n", "else", length);
      objSize = (4 + length)*sizeof(int);
    }
    //fprintf(stderr, "I am here.CCCCopy************** >>>>>>>> %x %d\n", objSize, objSize);
    //fprintf(stderr, "heap: %x\n", heap.size);
      //fprintf(stderr, "heap: %x\n", heap.from);
      //fprintf(stderr, "heap: %x\n", heap.fromFree);
      //fprintf(stderr, "heap: %x\n", heap.to);
      //fprintf(stderr, "heap: %x\n", heap.toStart);
      //fprintf(stderr, "heap: %x\n", heap.toNext);
    if(heap.toNext + objSize - heap.to > heap.size){
       fprintf(stderr, "ERROR: OutOfMemory!\n");
       exit(333);     
    }


    memcpy(heap.toNext, (char *)obj, objSize);
    
    *from_forwarding_Addr = heap.toNext;       /* update fowarding field */    
    ((char **)heap.toNext)[3] = heap.toNext; /* update fowarding field */
    // *(arguments_base_address + (argc - 1 - i)) = (int)heap.toNext; /* update argv[j] */
    heap.toNext += objSize;

    assert(heap.toNext - heap.to <= heap.size);
    return (int)*from_forwarding_Addr;
}

static void CopyObjectFields(int obj_Addr)
{
    //fprintf(stderr, "--------------------------------------@@@@@@@@@@ CopyObjectFields>>>>>\n");
    int * obj = (int *)obj_Addr;

    void * vptr = (void *)obj[0];
    int isObjOrArray = obj[1];
    int length = obj[2];
    // char ** from_forwarding_Addr = (char **)&obj[3];  /* &obj.forwarding in from space */
    int * fields = &obj[4];

    int size = GetObjSize(obj_Addr);
    if (GetObjType(obj_Addr) == 0){
      char * gc_map = *((char **)vptr);    
      int fieldCount = strlen(gc_map);
      
      //fprintf(stderr, "----------------- %s  %d >>>>>\n", gc_map, fieldCount);
      int i;    
      for(i = 0; i < fieldCount; i++){
        if(gc_map[i] == '1'){
          if (IsObjInitialized(fields[i]) && !IsInToSpace(fields[i])){
            fields[i] = CopyObject(fields[i]); /* update refer */
          }
        }
      }
    }
    heap.toStart += size;
    //fprintf(stderr, "-----------------CopyObjectFields +%d toStart:%x >>>>>\n", size, heap.toStart);

    assert(heap.toNext - heap.to <= heap.size);
    // return (int)*from_forwarding_Addr;
}




// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
static void Tiger_gc ()
{
  static int round = 0;
  clock_t start = clock();
  //fprintf(stderr, "--------------------------------------@@@@@@@@@@ Tiger_gc>>>>>\n");
  // Your code here:
  // return;

  // Init
  heap.toStart = heap.to;
  heap.toNext = heap.to;
  void * saved_prev = prev;
  while(prev != NULL){
    //fprintf(stderr, "------ prev: %x >>>>>\n", prev);
    dump_GC_frame((int)prev);
    dump_heap();
    void * gc_prev = *((void **)prev);
    char * arguments_gc_map = (char *)*((void **)prev + 1);
    int * arguments_base_address = (int *)*((void **)prev + 2);
    // char * locals_gc_map = (char *)*((void **)prev + 3);
    int localRefCount = *((int *)prev + 3);
    // //fprintf(stderr, "I am here.  %x\n", arguments_base_address);




    // args refs
    int argc = strlen(arguments_gc_map);
    int i;
    //fprintf(stderr, "argc: %d\n", argc);
    for (i = 0; i < argc; ++i){
      //fprintf(stderr, "arguments_gc_map = \"%s\"  [%d] = %c\n", arguments_gc_map, i, arguments_gc_map[i]);
      if(arguments_gc_map[i] == '1'){
        int obj_Addr = *(arguments_base_address + i); /* YKG. BUG FIXED! */
        //fprintf(stderr, "objAddr: %x\n", obj_Addr);
        if (IsObjInitialized(obj_Addr) && !IsInToSpace(obj_Addr)){
          
          // //fprintf(stderr, "+++++++++++++vvvv: %x\n", *((int *)obj_Addr));
          // //fprintf(stderr, "I am here.  addr:%x\n", obj_Addr);
          *(arguments_base_address + i) = CopyObject(obj_Addr); /* update argv[j] */
          // //fprintf(stderr, "I am here.333\n");
        }else{
          // //fprintf(stderr, ">>>>>>>>>>>>>>>>%x\n", obj_Addr);
        }
      }
    }

    //fprintf(stderr, ">>>>>>>>>>>>>>args finished2222!!!!!!\n");
    













    // local refs
    // int localc = strlen(locals_gc_map);    
    //fprintf(stderr, ">>>>>>>>>>>>>>localRefCount  %d\n", localRefCount);
    int * locals = (int *)((int *)prev + 4);       /* local ref start */
    for (i = 0; i < localRefCount; ++i){
        //fprintf(stderr, "locals[%d]: %x\n", i, locals[i]);
        if (!IsObjInitialized(locals[i])) continue;

        if (!IsInToSpace(locals[i])){
          locals[i] = CopyObject(locals[i]);
        }else{
          locals[i] = GetForwarding(locals[i]);
        }
        //fprintf(stderr, "locals[%d]: %x\n", i, locals[i]);
    }

    prev = (void *)(*((int **)prev));
    //fprintf(stderr, "Next prev:  %x\n", prev);
  }
  prev = saved_prev;
  //fprintf(stderr, ">>>>>>>>>>>>>>locals finished!!!!!!\n");










  //fprintf(stderr, "I am here.....................\n");
  // BFS
  // int t = 0;
  while(heap.toStart < heap.toNext){
      dump_heap();
        int front = (int)heap.toStart;
       CopyObjectFields((int)heap.toStart);
       assert(front != (int)heap.toStart);
       // if(++t > 8) exit(100);
  }

  //fprintf(stderr, "I am here.....................\n");
  dump_heap();
  clock_t stop = clock();
  // //fprintf(stderr, "GC: round %d, cost %d clocks  collected %d(0x%x) bytes. size: 0x%x\n", round, (stop - start),
  //       (heap.fromFree - heap.from) - (heap.toNext - heap.to), (heap.fromFree - heap.from) - (heap.toNext - heap.to), heap.size);  
  // printf("GC: round %d, cost %d clocks  collected %d(0x%x) bytes. size: 0x%x\n", ++round, (stop - start),
  //       (heap.fromFree - heap.from) - (heap.toNext - heap.to), (heap.fromFree - heap.from) - (heap.toNext - heap.to), heap.size);
  printf("GC: round %d, cost %fs  collected %d(0x%x) bytes.\n", ++round, (stop - start)*1.0/1000,
        (heap.fromFree - heap.from) - (heap.toNext - heap.to), (heap.fromFree - heap.from) - (heap.toNext - heap.to));
  swap(&heap.from, &heap.to);
  heap.fromFree = heap.toNext;
  heap.toNext = heap.toStart = heap.to;  
  dump_heap();
}



// static void Tiger_gc2 ()
// {
//   // Your code here:
//   while(prev != NULL){
//     void * gc_prev = *((void **)prev);
//     char * arguments_gc_map = (char *)*((void **)prev + 1);
//     int * arguments_base_address = (int *)*((void **)prev + 2);
//     char * locals_gc_map = (char *)*((void **)prev + 3);
//     int localRefCount = *((int *)prev + 4);

//     int argc = strlen(arguments_gc_map);
//     int i;
//     for (i = 0; i < argc; ++i)
//     {
//       if(arguments_gc_map[i] == '1'){
//         void * obj;    
//         int objSize;
//         int fieldCount;
//         void * vptr;
//         int isObjOrArray;
//         char ** from_forwarding_Addr; /* &obj.forwarding in from space */

//         obj = (void *)*(arguments_base_address + (i-1));
//         isObjOrArray = *((int *)obj + 1);
//         if (isObjOrArray == 0){  /* YKG. normal object */        
//           vptr = (void *)*(arguments_base_address + (i-1));
//           fieldCount = strlen(*(char **)vptr); /* strlen(field_gc_map) */
//           objSize = (4 + fieldCount)*sizeof(int); /* 4 for obj header */

//           memcpy(heap.toStart, obj, objSize);
//           from_forwarding_Addr = (void *)*(arguments_base_address + (i-1));
//           heap.toStart += objSize;

//         }

//       }
//     }
//     while(argc-- >= 0){
//       if(arguments_gc_map + )
//       arguments_base_address + argc
//     }
//     heap.toStart 

//     prev = prev->__gc_prev;
//   }
// }


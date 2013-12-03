#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

static void Tiger_gc ();
static int GetFreeSpace();
// void dump_Obj_Header(int addr);

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
  // heap.toStart = NULL;
  heap.toStart = heap.to; /* YKG. Why NULL? */

  // #7: initialize the "toNext" field with NULL;
  heap.toNext = NULL;
  heap.toNext = heap.to; /* YKG. Why NULL? */

  return;
}

void dump_heap()
{
  fprintf(stderr, "=====================================\n");
  fprintf(stderr, "%8x heap.size \n", heap.size);
  fprintf(stderr, "%8x heap.from \n", heap.from);
  fprintf(stderr, "%8x heap.fromFree \n", heap.fromFree);
  fprintf(stderr, "%8x heap.to \n", heap.to);
  fprintf(stderr, "%8x heap.toStart \n", heap.toStart);
  fprintf(stderr, "%8x heap.toNext \n", heap.toNext);
  fprintf(stderr, "%8x %d::: heap.usedFrom \n", heap.fromFree - heap.from, heap.fromFree - heap.from);
  fprintf(stderr, "%8x ::: heap.usedTo \n", heap.toNext - heap.to);
}


// The "prev" pointer, pointing to the top frame on the GC stack. 
// (see part A of Lab 4)
void *prev = 0;


//===============================================================//
// Object Model And allocation

typedef struct 
{
  void ** vptr;
  int isObjOrArray;
  int length;
  char * forwarding;
} ObjHeader;

typedef struct {
  void *__gc_prev;                      // dynamic chain, pointing to f's caller's GC frame
  char *arguments_gc_map;         // should be assigned the value of "f_arguments_gc_map"
  int *arguments_base_address;    // address of the first argument
  int localRefCount;
} GC_FrameHeader;

enum {
  NORMAL,
  INTARRAY
};

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
// void *Tiger_new (void *vtable, int size)
void *Tiger_new (void **vtable, int size)   /* YKG. I modified the function prototype. */
{
  // // You should write 4 statements for this function.
  // // #1: "malloc" a chunk of memory of size "size":
  if (GetFreeSpace() < size){
    Tiger_gc();
  }
  ObjHeader * obj = (ObjHeader *)heap.fromFree;
  heap.fromFree += size;
  // dump_heap();

  // #2: clear this chunk of memory (zero off it):
  memset(obj, 0, size);

  // #3: set up the "vtable" pointer properly:
  obj->vptr = vtable;
  obj->isObjOrArray = NORMAL;
  obj->forwarding = (char *)obj;

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
  int size = (length + 4) * sizeof(int);
  if (GetFreeSpace() < size){
    Tiger_gc();
  }  
  ObjHeader * obj = (ObjHeader *)heap.fromFree;
  heap.fromFree += size;
  // dump_heap();

  // #2: clear this chunk of memory (zero off it):
  memset(obj, 0, size);

  obj->isObjOrArray = INTARRAY;
  obj->length = length;
  obj->forwarding = (char *)obj;
  return obj;
}


//===============================================================//
// The Gimple Garbage Collector

static int GetFreeSpace(){
  return heap.size - (heap.fromFree - heap.from);
}

static void swap(void *a, void *b)
{
  int aa, bb;

  aa = *((int *)a);
  bb = *((int *)b);

  *((int *)a) = bb;
  *((int *)b) = aa;  
}

static int MIN(int a, int b)
{
  return a < b ? a : b;
}
static int MAX(int a, int b)
{
  return a > b ? a : b;
}

static int GetFieldCount(int obj_Addr)
{
  void * vptr = ((ObjHeader *)obj_Addr)->vptr;
  char * gc_map = *((char **)vptr);   
  return strlen(gc_map);
}


static int GetObjSize(int obj_Addr)
{
  ObjHeader * obj = (ObjHeader *)obj_Addr;
  if(obj->isObjOrArray == NORMAL){
    return (4 + GetFieldCount(obj_Addr))*sizeof(int);
  }else{
    int length = *((int *)obj_Addr + 2);
    return (4 + length)*sizeof(int);
  }  
}


static int IsObjAllocated(int obj_Addr)
{
  if (obj_Addr){
    int min = MIN((int)heap.from, (int)heap.to);
    int max = MAX((int)heap.from, (int)heap.to) + heap.size;
    assert(min <= obj_Addr && obj_Addr < max);
  }
  
  return obj_Addr != 0; /* YKG. NULL indicate the object has not be allocated */
}


static int IsInToSpace(int obj_Addr)
{
  ObjHeader * obj = (ObjHeader *)obj_Addr;
  if (obj->forwarding != (char *)obj) return 1;

  return (int)heap.to <= obj_Addr && obj_Addr < (int)heap.toNext;
}

static int CopyObject(int obj_Addr)
{
  ObjHeader * obj = (ObjHeader *)obj_Addr;
  int size = GetObjSize(obj_Addr);
  
  assert(heap.toNext + size - heap.to <= heap.size);

  memcpy(heap.toNext, (char *)obj, size);  
  obj->forwarding = heap.toNext;  /* update origin fowarding field */    
  ((ObjHeader *)heap.toNext)->forwarding = heap.toNext; /* update new obj fowarding field */
  heap.toNext += size;
  // fprintf(stderr, ">>> size:%d %x\n", size, size);
  // dump_heap();

  return (int)obj->forwarding;
}

static void CopyObjectFields(int obj_Addr)
{ 
  ObjHeader * obj = (ObjHeader *)obj_Addr;
  int size = GetObjSize(obj_Addr);
  if (obj->isObjOrArray == NORMAL){
    int * fields = (int *)(obj + 1);
    char * gc_map = *((char **)(obj->vptr));
    int fieldCount = strlen(gc_map);
    int i;

    for(i = 0; i < fieldCount; i++){
      if(gc_map[i] == '1' && IsObjAllocated(fields[i]) && !IsInToSpace(fields[i])){
          fields[i] = CopyObject(fields[i]); /* update refer */        
      }
    }
  }
  heap.toStart += size;
}


// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
static void Tiger_gc ()
{
  // fprintf(stderr, "gc!!!!!!!!!!!!!!!!!!!!\n");
  static int round = 0;
  int before_space, after_space, i;
  clock_t start = clock();
  before_space = GetFreeSpace();

  // Init queue
  void * saved_prev = prev;
  while(prev != NULL){
    GC_FrameHeader * frame = (GC_FrameHeader *)prev;

    // args refs
    int argc = strlen(frame->arguments_gc_map);
    for (i = 0; i < argc; ++i){
      if(frame->arguments_gc_map[i] == '1'){
        int obj_Addr = *(frame->arguments_base_address + i); /* YKG. BUG FIXED! */        
        if (IsObjAllocated(obj_Addr) && !IsInToSpace(obj_Addr)){
          *(frame->arguments_base_address + i) = CopyObject(obj_Addr); /* update argv[j] */
        }
      }
    }

    // local refs
    int * locals = (int *)(frame + 1);       /* local ref start */
    for (i = 0; i < frame->localRefCount; ++i){        
        if (!IsObjAllocated(locals[i])) continue;
        locals[i] = IsInToSpace(locals[i]) ? 
            (int)((ObjHeader *)locals[i])->forwarding : CopyObject(locals[i]);
    }

    prev = (void *)frame->__gc_prev;
  }
  prev = saved_prev;

  // BFS
  while(heap.toStart < heap.toNext){
    CopyObjectFields((int)heap.toStart);
  }  
  swap(&heap.from, &heap.to);
  heap.fromFree = heap.toNext;
  heap.toNext = heap.toStart = heap.to;
  after_space = GetFreeSpace();
  clock_t stop = clock();

  if(before_space == after_space){
    fprintf(stderr, "ERROR: OutOfMemory!\n");
    exit(3);
  }
  fprintf(stderr, "GC: round %d, cost %fs  collected %d(0x%x) bytes.\n",
      ++round, (stop - start)*1.0/1000, after_space - before_space, after_space - before_space);
}


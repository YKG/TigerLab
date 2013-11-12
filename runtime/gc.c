#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    -----------------------------------------
      | vptr | v0 | v1 | ...      | v_{size-1}|
      -----------------------------------------
      ^      \                                /
      |       \<------------- size --------->/
      |
      p (returned address)
*/
void *Tiger_new (void *vtable, int size)
{
  // You should write 4 statements for this function.
  // #1: "malloc" a chunk of memory of size "size":
  void * obj = malloc(size);
  if(obj < 0){
	  fprintf(stderr, "malloc failed!");
	  exit(1);
  }
  // #2: clear this chunk of memory (zero off it):
  memset(obj, 0, size);
  // #3: set up the "vtable" pointer properly:
  *((int **)obj) = (int *)vtable;  /* YKG. Consider the vptr is the first field of the struct. */
  // #4: return the pointer
  return obj;
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length.
// This function should return the starting address
// of the array elements, but not the starting address of
// the array chunk.
/*    ---------------------------------------------
      | length | e0 | e1 | ...      | e_{length-1}|
      ---------------------------------------------
               ^
               |
               p (returned address)
*/
void * Tiger_new_array (int length)
{
  // You can use the C "malloc" facilities, as above.
  // Your code here:
  void * arr = malloc((length + 1) * sizeof(int));
  if(arr < 0){
	  fprintf(stderr, "malloc failed!");
	  exit(1);
  }
  // #2: clear this chunk of memory (zero off it):
  memset(arr, 0, (length + 1) * sizeof(int));
  *((int *)arr) = length;
  return (void *)(((int *)arr) + 1);
}



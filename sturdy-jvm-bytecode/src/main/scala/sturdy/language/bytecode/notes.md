Object Notes

New objects:

New instruction puts a ObjectType on the Stack  
DUP after   
Object Parameters after that    
Invoke init method, then store the object

init:   
Aload the Object ref that was put on the stack (object itself given as parameter basically
Invoke java.lang.Object init (do we need that?)     
Aload again     
initialize field with default values, after with parameters given to init, nothing special here, aload after every put, put consumes the object on the stack        
paramters taken from locals, nothing special

invokevirtual:  
same as invokestatic, just consumes a object from the stack, or does it?     
invokespecial seems to be identical to invokevirtual    

Invokeinstructions have no reference to the method they are invoking, just the name, signature and declaring class  
method can be found in the declaring class via the name and signature

Opal has two representations for Objects:
ObjectType, basically just the name string of the object    
ClassFile, the object with all the bells and whistles, fields, methods, etc..   
Can find ClassFile via the ObjectType in the Project created by reading a .jar

little weirdness about popped objects, in bytecode they are passed as params, but not in OPAL, where they are just suddenly stored in local_0   
^ this is the representation of **this**, basically in OPAL all methods of a object have the object as local_0    
So we dont actually have to put objects on the stack? hmmm, wait how does this work with subclasses, and fields?
# Groovy Jupyter Kernel

A native [Jupyter](http://jupyter.org) kernel for the [Apache Groovy](http://www.groovy-lang.org) language.

By "native" we mean the kernel is written in Groovy and handles
the ZeroMQ message queues directly.

## Installation

Copy the *kernel.jar* and *kernel.json* files to Jupyter's kernel directory.  Jupyter
will search for kernels in several locations and you can use the 
`jupyter --paths` command to see where Jupyter is looking on your machine.
This page assumes */usr/local/share/jupyter*.

Edit the *kernel.json* file and modify the path in the *argv* field to point
to the *kernel.jar* file location.  This page assumes the jar file and the json
file reside together in the same directory, but this is not required.
Nothing else in the *kernel.json* file should be changed.

```
{
	"argv": [ "java", "-jar", "/usr/local/share/jupyter/kernels/groovy/kernel.jar", "{connection_file}" ],
	"display_name": "Groovy",
	"language": "groovy",
	"env": { "PS1":"groovy>"}
}
```

The option to start a Groovy kernel should now be available in Jupyter.

## Creating a Kernel for your DSL

The Groovy kernel uses objects that implement the `org.lappsgrid.jupyter.groovy.context.GroovyContext` interface
to configure the Groovy compiler and to obtain `MetaClass` instances that
are attached to the compiled script objects.
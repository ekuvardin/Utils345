# Solution-template

Solution examples 
   main.SortBigFile - example of K-way merge big file
          In order to launch program call main.sortBigFile.SortInteger
          with parameters
              chk  - max count of Integer elements that can we read from array
              ct   - max count of chunks. So then total array will consists from chk * ct elements
              pl   - max size of threads taking part in sorting
              inpf - name of input file. So you can use main.Generator to get big file
              resf - name of final result file
              pm   - use parallel merge. Available values true/false
              
   main.ProducerConsumer - simple solution of Producer-Consumer problem. Run Worker to see results
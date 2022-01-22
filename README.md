# ZosShell

## Demo shows most of the common commands functionality:

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/main-demo.gif)

## Demo shows download (downloading member(s) from a z/OS instance to your local PC drive) functionality:

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/download-demo.gif)

## Demo shows vi (editing a member and saving it back to the z/OS instance) functionality:

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/save-demo.gif)
  
This project demonstrates the usage of [Zowe Java Client SDK](https://github.com/zowe/zowe-client-java-sdk).
  
Project provides a UI shell command prompt that allows you to manipulate datasets and its members. The shell performs the following linux like commands:  
  
    cat                     - display contents
    cd <arg>                - where arg is a dataset value or empty
    h | help                - list commands
    history <arg>           - where arg is optional and indicates the number to display from bottom   
    !n                      - where n is a number, to execute command number n in history list   
    !string                 - will execute the last history command starting with that “string”
    ls <arg>                - where arg is a dataset value or empty 
    ls -l <arg>             - where arg is a dataset value or empty 
    ps                      - display all processes running
    ps <arg>                - where arg is a task/job name   
    pwd                     - show current working dataset
    rm <arg>                - where arg is "*", member, member with wildcard "*", dataset, or dataset with member value
    touch <arg>             - create member arg if it does not already exist
    uname                   - show current connected host name
    vi <arg>                - where arg is a sequential dataset or member name, arg will be downloaded 
                              and displayed for editing, use save command to save changes  
    whoami                  - show current connected user name
  
Along with following custom commands:  

    browsejob <arg1> <arg2>       - where arg1 is a job name and arg2 is optional
                                    if arg2 not specified, display job's JESMSGLG spool output
                                    if arg2 is equal to "all", display all job's spool output
    cancel <arg>                  - where arg is a task/job name  
    change <arg>                  - where arg is a number representing a connection
    color <arg>                   - change color of prompt an text, arg i.e. blue, yellow, cyan etc..
    connections                   - a list of connection(s)   
    count members                 - return member count in current pwd dataset
    count datasets                - return dataset count in current pwd dataset
    cp | copy <arg> arg>          - where arg can be ".", "*", member, dataset or dataset(member)
    download <arg>                - download arg to local c:\ZosShell\pwd where arg is member or sequential dataset
    end                           - end session closes shell UI window
    files                         - list all files under local pwd drive value
    save <arg>                    - save arg where arg is a file name from files command to the current pwd
    search <arg>                  - search for arg within a job log from the last browsejob or tailjob command output  
    stop <arg>                    - where arg is a task/job name  
    submit <arg>                  - where arg is a member name  
    tailjob <arg1> <arg2> <arg3>  - where arg1 is job name and arg2 and arg3 are optional
                                    use arg2 to specify either line limit or "all" value 
                                    if "all" is specified, display output from all of job's spool content
                                    line limit is 25 by default if not specified in arg2
    v | visited                   - a list of visited datasets  
  
The following key combinations provide the following functionality within the shell:  
  
    CTRL C                  - copy text
    CTRL V                  - paste coped text
    UP arrow                - scroll up through history list
    DOWN arrow              - scroll down through history list
    
To quit from the command shell UI, you can either press 'X' windows close icon or enter 'end' keyword.  
  
## Requirements  
  
    Java 11+ 
    Maven
    z/OSMF installed on your backend z/OS instance.
              
## Build And Execute  

Create a credentials.txt file under "C:\ZosShell" drive\directory location that contains a list of z/OSMF connections per line with a comma delimiter for
connection values. You can change the drive\directory location by changing the hard coded value in the code.    
  
Format:  
    
    hostname,zomsfportnumber,username,password  
    
At the root directory prompt, execute the following maven command:  
  
    mvn clean install  
  
Change directory to the target directory and execute the following command:  
  
    java -jar zosshell-1.0.jar   
  
If you are planning to browse large job output you may want to set the JVM memory usage higher than the default, i.e.  
  
    java -jar -Xmx2G zosshell-1.0.jar   
  
### Terminal color configuration (optional)
  
By default, the terminal will display its text in green on a black background. If you want to change those settings, follow the instructions bellow.  
  
Create a colors.txt file under "C:\ZosShell" drive\directory location that contains one line of two comma delimiter values to control the color scheme.    
  
First value will control the color of the text and prompt. Second value will control the color of the background panel.  
  
Format:  
  
    colornameornumbervalue,colornameornumbervalue  
  
Example:  
  
    449,white

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/color.gif)    
  
  

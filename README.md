# ZosShell  
  
ZosShell provides a client like Linux shell to manipulate members and datasets on your z/OS backend.  
  
The project arise for a need for something more simple, direct and less verbosity than Zowe CLI.     
  
The app works well on Windows and macOS. It has not been tested on a Linux environment and some features may be limited.         
    
Project demonstrates the usage of [Zowe Client Java SDK](https://github.com/zowe/zowe-client-java-sdk).  
        
## Main Demo  
  
![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/main-demo.gif)

## Increase Front Size Demo

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/increase-size.gif)

## Download Demo  

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/download-demo.gif)

## Edit/Save Member Demo   
  
![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/save-demo.gif)
      
The shell performs the following Linux like commands:    
    
    cat                     - display contents
    cd <arg>                - where arg is a dataset value or empty
    clear                   - clear the shell of all history
    cp | copy <arg> <arg>   - where arg can be ".", "*", member, dataset or dataset(member)
    env                     - display environment variables
    g | grep <arg> <arg2>   - where arg is search string and arg2 is member value
    h | help                - list commands
    history <arg>           - where arg is optional and indicates the number to display from bottom  
    hostname                - display current hostname connection
    !n                      - where n is a number, to execute command number n in history list   
    !string                 - will execute the last history command starting with that “string”
    !!                      - will execute the last history command
    ls <arg>                - where arg is optional and indicates a dataset or member value
                            - for member value only you can specified * wild card as last character
    ls -l <arg>             - where arg is optional and indicates a dataset or member value
                            - for member value only you can specified * wild card as last character
    mkdir <arg>             - where arg is a dataset
    ps                      - display all processes running
    ps <arg>                - where arg is a task/job name   
    pwd                     - show current working dataset
    rm <arg>                - where arg is "*", member, member with wildcard "*", dataset, or dataset with member value
    set <arg>               - set environment variable with arg value in the following format: key=value
    touch <arg>             - create member arg if it does not already exist
    uname                   - show current connected host name
    vi <arg>                - where arg is a sequential dataset or member name, arg will be downloaded 
                              and displayed for editing, use save command to save changes  
    whoami                  - show current connected user name
  
Along with following custom commands:  

    bj | browsejob <arg1> <arg2>        - where arg1 is a job name and arg2 is optional
                                          if arg2 not specified, display job's JESMSGLG spool output
                                          if arg2 is equal to "all", display all job's spool output
    cancel <arg>                        - where arg is a task/job name  
    change <arg>                        - where arg is a number representing a connection
    clearlog                            - clear out the cached job log from last browsejob command 
    color <arg> <arg2>                  - change color arg is prompt and text and arg2 is background color, i.e. blue, yellow, cyan etc..
    connections                         - a list of connection(s)   
    count members                       - return member count in current pwd dataset
    count datasets                      - return dataset count in current pwd dataset
    cps | copys <arg> arg>              - where at least one argument is a sequential dataset
                                        - for sequential dataset copying
    d | download <arg1> <arg2>          - download arg1 to local c:\ZosShell\pwd where arg1 is member or sequential dataset  
                                          and arg2 is optional and only accepts "-b" for binary download      
    dj | downloadjob <arg1> <arg2>      - download the latest job log where <arg1> is job name
                                          if arg2 not specified, download job's JESMSGLG spool output
                                          if arg2 is equal to "all", download all job's spool output
    end                                 - end session closes shell UI window
    files                               - list all files under local pwd drive value
    ls --l <arg>                        - where arg is optional and indicates a dataset or member value
                                        - for member value only you can specified * wild card as last character
                                        - -- option means same view as ls -l but without attribute info
    mvs <arg>                           - execute a mvs command where arg is a command string within double quotes
    pj | purgejob <arg>                 - purge a job name or job is arg can represent either  
    save <arg>                          - save arg where arg is a file name from files command to the current pwd
    search <arg>                        - search for arg within last job browse, tailjob or member cat command  
    stop <arg>                          - where arg is a task/job name  
    submit <arg>                        - where arg is a member name  
    tj | tailjob <arg1> <arg2> <arg3>   - where arg1 is job name and arg2 and arg3 are optional
                                          use arg2 to specify either line limit or "all" value 
                                          if "all" is specified, display output from all of job's spool content
                                          line limit is 25 by default if not specified in arg2
    t | timeout <arg>                   - where arg is optional, with arg value you set new timeout, without shows current value
    tso <arg>                           - execute a tso command where arg is a command string within double quotes
    ussh <arg>                          - execute a uss (unix) command via SSH connection where arg is a command string within double quotes
    v | visited                         - a list of visited datasets  
  
Key combinations provide the following functionality within the shell:    
    
    CTRL C                  - copy text
    CTRL V                  - paste coped text
    UP arrow                - scroll up through history list
    DOWN arrow              - scroll down through history list
    SHIFT UP arrow          - increase font size
    SHIFT DOWN arrow        - decrease font size
    TAB                     - command autofill key - type a few characters then click on TAB key 
    
To quit from the command shell UI, you can either press 'X' windows close icon or enter 'end' keyword.  
  
## Requirements  

    Maven
    Compatible with all Java versions 11 and above.
    z/OSMF installed on your backend z/OS instance.
              
## Build And Execute  

Create a credentials.txt file under "C:\ZosShell" for Windows or "/ZosShell" on Max OSX directory that contains a list of z/OSMF connections per line with a comma delimiter for
connection values. You can change the drive\directory location by changing the hard coded value in the code.    
  
Format:  
    
    hostname,zomsfportnumber,username,password,mvsconsolename,sshport  
  
or
  
    hostname,zomsfportnumber,username,password,sshport  
  
NOTES:  
  
"mvsconsolename" is optional. If executing a mvs command fails, then your zosmf instance may be using a console name other than the default. If so, you can specify the different mvsconsolename.      
    
"sshport" is optional. If not specified, "ussh" command will fail.    
          
At the root directory prompt, execute the following maven command:  
  
    mvn clean install  
  
Change directory to the target directory and execute the following command:  
  
    java -jar zosshell-1.0.jar   
  
If you are planning to browse large job output you may want to set the JVM memory usage higher than the default, i.e.  
  
    java -jar -Xmx2G zosshell-1.0.jar   
  
### Terminal configuration properties (optional)
  
By default, the terminal will display its text in green on a black background. If you want to change those settings and more, follow the instructions bellow.  
  
Create a config.txt file under "C:\ZosShell" for Windows or "/ZosShell" on Max OSX directory location that contains one line of two comma delimiter values.  
  
Config property values specify in order on the same line separated by comma.   
   
    First value will set the following properties input.color and prompt.color
    Second value will control the color of the background panel.
    Third value will set the following properties: input.font.size and prompt.font.size
    Forth value will trigger text to be bold   
  
Format:  
  
    colornameornumbervalue,colornameornumbervalue,fontnumbersizenumbervalue,anyvalue  
  
Example:  
  
    yellow,green,19,yes

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/colors.gif)    

## Trouble Shooting
    
log4j2 logging is set up and configuration is located under src/main/resources/log4j2.xml  
    
It is configured to produce output logging while application is running under the running directory where the application was kicked off.
      
You are free to change configuration accordingly for your needs. 
  
    
  
  

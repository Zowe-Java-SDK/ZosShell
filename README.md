# ZosShell  
  
ZosShell provides a client like Linux shell to perform z/OS system services commands against a z/OS instance.  
  
The commands exploit the z/OSMF Rest API layer installed on a z/OS instance. 
  
The ZosShell application utilizes the [Zowe Client Java SDK](https://github.com/zowe/zowe-client-java-sdk) library to call the Rest APIs.  
  
With ZosShell and the functionality it provides with the dependencies noted you can perform the following commands:

    MVS console  
    TSO console  
      
    Dataset/Member:  
    list, view, create, delete, and edit. 
    
    Job/StartedTask:   
    list, submit, start, stop, cancel, purge, monitor and browse   
      
    Download: sequential dataset, member, and job/started task log. 
        
The project arise for a need for something more simple, direct and less verbosity than [Zowe CLI](https://github.com/zowe/zowe-cli) for the most common commands.       
  
The app works well on Windows and macOS.   
  
Project demonstrates the usage of [Zowe Client Java SDK](https://github.com/zowe/zowe-client-java-sdk).  
        
## Main Demo  
  
![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/main-demo.gif)

## Increase Font Size Demo

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/increase-size.gif)

## Download Demo  

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/download-demo.gif)

## Edit/Save Member Demo   
  
![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/save-demo.gif)
      
The shell performs the following Linux like commands:    
    
    cat                     - display contents
    cd <arg>                - where arg is a dataset value or empty
    clear                   - clear the shell of all history and cached output for search command
    cp | copy <arg> <arg>   - where arg can be ".", "*", member, sequential dataset or dataset(member)
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
    rm <arg>                - where arg is member with wildcard "*", member, dataset, or dataset with member value
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
    color <arg> <arg2>                  - change color arg is prompt and text and arg2 is background color, i.e. blue, yellow, cyan etc..
    connections                         - a list of connection(s)   
    count members                       - return member count in current pwd dataset
    count datasets                      - return dataset count in current pwd dataset
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
    p | purge <arg>                     - purge a job name or job is arg can represent either  
    save <arg>                          - save arg where arg is a file name from files command to the current pwd
    search <arg>                        - search for arg within last job browse, tailjob or member cat command  
    stop <arg>                          - where arg is a task/job name  
    submit <arg>                        - where arg is a member name  
    tail <arg1> <arg2> <arg3>           - where arg1 is job name and arg2 and arg3 are optional
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
          
At the root directory prompt, execute the following maven command:  
  
    mvn clean install  
  
Change directory to the target directory and execute the following command:  
  
    java -jar zosshell-2.0.jar   
  
If you are planning to browse large job output you may want to set the JVM memory usage higher than the default, i.e.  
  
    java -jar -Xmx2G zosshell-2.0.jar   
  
### Terminal configuration properties
  
By default, the configuration file name is config.json located within C:\ZosShell directory for Windows or /ZosShell for macOS.  
  
You can override the default file name and its location by setting the following OS environment variable:  
  
    ZOSSHELL_CONFIG_PATH  
  
The configuration file consists of JSON data. The configuration JSON string is defined as a JSON array structure. The array will consist of one or more profile(s).
  
A profile is a one-to-one relationship of Profile.java file within the project. It contains variables as a placeholder for configuration information, such as z/OSMF and ssh connection information and properties to control the Window environment.  
    
In addition, each profile contains path variable to control location for download directory.  

The first JSON array entry in the example below shows all the attributes defined to be read by the application.  
  
The other JSON array entries shows that you don't need to specify all attributes and its values. The attributes most critical are those that specify a z/OSMF connection: hostname, zosmfport, username, and password.  
  
Example of config.json:  

    [
        {
            "hostname": "xxxxxxxxx",
            "zosmfport": "xxxx",
            "sshport" : "xxxx",
            "username": "xxxxx",
            "password": "xxxxxx",
            "downloadpath": "/ZosShell",
            "consolename": "",
            "window": {
                        "fontsize": "xxx",
                        "fontbold": "xxxxx",
                        "textcolor": "xxxx",
                        "backgroundcolor": "xxx"
                      }		
        },
        {
            "hostname": "xxxxxxxxx",
            "zosmfport": "xxxx",
            "sshport" : "xxxx",
            "username": "xxxxx",
            "password": "xxxxxx",
            "downloadpath": "C:\\ZosShell3",
            "consolename": "",
            "window": {}		
        },
        {
            "hostname": "xxxxxxxxx",
            "zosmfport": "xxxx",
            "sshport" : "xxxx",
            "username": "xxxxx",
            "password": "xxxxxx",
            "downloadpath": "C:\\ZosShell",
            "consolename": "",
            "window": {}		
        }
    ]
  
JSON configuration file is required for the application to work properly. Any error in finding the file or parsing the JSON string will result in the application being unusable; it will display an error and any input will close the app.    
    
The following screenshot displays ZosShell shell window with custom windows properties defined within the "window" JSON section.  
  
Here the window is set to display its background in green and font as yellow/bold.   
  
![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/colors.gif)    

## Troubleshooting
    
Logging framework log4j2 is configured for the project. log4j2 configuration is located under src/main/resources/log4j2.xml.  
    
It is configured to produce output logging while application is running under the running directory where the application was kicked off.
      
You are free to change configuration accordingly for your needs. 
  
    
  
  

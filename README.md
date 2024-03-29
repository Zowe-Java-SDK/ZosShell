# ZosShell  
  
ZosShell provides a client like Linux shell to perform z/OS system service commands.  
  
The commands exploit the z/OSMF Rest API layer on z/OS. 
  
ZosShell acts like a Bash shell. It provides similar bash concepts and processing. Examples of shell functionality are: keeping track of command history, changing directories represented as a partition dataset, history shortcuts with exclamation mark, auto command type ahead with TAB key and much more. It also caches each command output that can be searched by the search command.  

The project arose because of a need for something more simple, direct and less verbiage than [Zowe CLI](https://github.com/zowe/zowe-cli) for the most common commands. 
  
ZosShell demonstrates the usage of [Zowe Client Java SDK](https://github.com/zowe/zowe-client-java-sdk).    
    
Zowe Client Java SDK provides the plumbing to perform z/OSMF Rest API calls.     
  
With ZosShell, the following z/OS service commands can be performed:  
  
MVS
  
    MVS console command

TSO  

    TSO console command

Member  

    copy
    create
    delete
    download
    edit
    list
    rename
    save
    view

Partition Dataset (PDS)

    copy
    create
    delete
    list
    rename
    view

Sequential Dataset  

    copy
    create
    delete
    download
    edit
    list
    rename
    save
    view

Job and StartedTask      

    cancel
    browse log
    download log
    list
    monitor
    purge
    start
    stop
    submit  
  
The app works well on Windows and macOS.   
          
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
    clear                   - clear the shell screen and start at top and empty out cached output for search command
    cp | copy <arg> <arg>   - where arg can be ".", "*", member, sequential dataset or dataset(member)
    echo <arg>              - display given input and translate any env value delimited with $
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
    touch <arg>             - create empty member if does not exist, arg represents a member or dataset(member)
    uname                   - show current connected host name
    usermod <arg>           - modify username or password of current connection, arg can be either -u or -p
    vi <arg>                - where arg is a sequential dataset or member name, arg will be downloaded 
                              and displayed for editing, use save command to save changes  
    whoami                  - show current set connection's username  
  
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
    rn | rename <arg> <arg>             - rename sequential dataset or member, both arg values either member or dataset type
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
  
    java -jar zosshell-2.1.1.jar   
  
If you are planning to browse large job output you may want to set the JVM memory usage higher than the default, i.e.  
  
    java -jar -Xmx2G zosshell-2.1.1.jar   
  
### Terminal configuration properties
  
By default, the configuration file name is config.json located within C:\ZosShell directory for Windows or /ZosShell for macOS.  
  
You can override the default file name and its location by setting the following OS environment variable:  
  
    ZOSSHELL_CONFIG_PATH  
  
The configuration file consists of JSON data. The configuration JSON string is defined as a JSON array structure. The array will consist of one or more profile(s).
  
A profile is a one-to-one relationship of [Profile.java](https://github.com/Zowe-Java-SDK/ZosShell/blob/master/src/main/java/zos/shell/singleton/configuration/model/Profile.java) file within the project. It contains variables as a placeholder for configuration information, such as z/OSMF and SSH connection information, properties to control the Window environment and much more.  
   
The first JSON array entry in the example below shows all the attributes defined to be read by the application.  
  
The other JSON array entries shows that you don't need to specify all attributes and its values. The attributes required are those that specify a z/OSMF connection: hostname and zosmfport.    
   
The username and password entries are optional. It is recommended to not specify those settings. When not specified, the application will prompt the end user for username and password for the current connection.   
    
For further details on username and password usage see [here](https://github.com/Zowe-Java-SDK/ZosShell/issues/182).    
    
Example of config.json:  

    [
        {
            "hostname": "xxxxxxxxx",
            "zosmfport": "xxxx",
            "sshport" : "xxxx",
            "username": "",
            "password": "",
            "downloadpath": "/ZosShell",
            "consolename": "",
            "accountnumber": "12345",
            "prompt": "$",
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
            "downloadpath": "C:\\ZosShell3",
            "consolename": "",
            "accountnumber": "",
            "prompt": "",
            "window": {}		
        },
        {
            "hostname": "xxxxxxxxx",
            "zosmfport": "xxxx",
            "sshport" : "xxxx",
            "downloadpath": "C:\\ZosShell",
            "consolename": "",
            "accountnumber": "",
            "prompt": "$(hostname)",
            "window": {}		
        }
    ]
  
Explanation of some of the variable settings:
  
    downloadpath specifies the location on your disk drive to store downloaded data
    consolename specifies the console name to use to perform MVS console command
    accountnumber specifies the account number needed to perform TSO command
    prompt specifies value to display for the application prompt
   
The following JSON variable settings are converted into environmental variables within the shell:
  
    hostname as HOSTNAME
    downloadpath as DOWNLOAD_PATH
    consolename as CONSOLE_NAME
    accountnumber as ACCOUNT_NUMBER
    prompt as PROMPT
  
Each of these environmental variables will appear at app startup via ENV command if any have a value specified within the configuration JSON file.

Use SET command to define or change each environmental variable noted above as needed and the new settings will be used by the application accordingly.
  
<b>PROMPT</b> setting controls what the shell prompt value should represent. By default, the prompt value is ">".  
  
A prompt can be set and changed directly with the SET command. It can parse other ENV variables' value to use within its prompt value.  
  
For example:  
  
    > env
    DOWNLOAD_PATH=/ZosShell
    HOSTNAME=hostname1
    > set prompt=$(hostname)
    prompt=$(hostname)
    hostname1> set prompt=start$(info)
    prompt=start$(info)
    START$(INFO)> set info=with
    info=with
    STARTWITH> 
  
JSON configuration file is required for the application to work properly. Any error in finding the file or parsing the JSON string will result in the application being unusable; it will display an error and any input will close the app.    
    
The following screenshot displays ZosShell shell window with custom windows properties defined within the "window" JSON section.  
  
Here the window is set to display its background in green and font as yellow/bold.   
  
![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/colors.gif)    

## Troubleshooting
    
Logging framework log4j2 is configured for the project. log4j2 configuration is located under src/main/resources/log4j2.xml.  
    
It is configured to produce output logging while application is running under the running directory where the application was kicked off.
      
You are free to change configuration accordingly for your needs. 
  

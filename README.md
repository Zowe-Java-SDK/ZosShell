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
      
The shell performs the following Linux-like commands:    
    
    cat <arg>               - arg=member, display contents
    cd <arg>                - arg is a dataset value or ..
    cls | clear             - clear screen contents and search cache
    cp | copy <arg> <arg>   - arg can be ".", member, dataset or dataset(member)
    echo <arg>              - echo arg value and translate any env value delimited with $
    env                     - echo all env variables
    g | grep <arg> <arg2>   - arg is search string and arg2 is member value
    h | help <arg>          - list all commands-details; arg=-l, list all command names; arg=command, list command-detail 
    history <arg>           - arg is optional and indicates the number to display from bottom 
    hostname                - echo hostname connection
    !n                      - n=number, execute command from history list   
    !string                 - execute previous string=command from history list
    !!                      - execute previous command
    ls <arg>                - arg is optional; list members/datasets in PWD; arg=filter(*); filter is dataset or member value
    ls -l <arg>             - arg is optional; list members/datasets with attributes in PWD; arg=filter(*); filter is dataset or member value
    mkdir <arg>             - arg is a dataset
    ps <arg>                - arg is optional; list all tasks/jobs; arg=filter(*)   
    pwd                     - current working dataset location
    rm <arg>                - arg is member with wildcard "*", member, dataset, or dataset(member)
    set <arg>               - set environment variable with arg value in the following format: key=value
    touch <arg>             - create empty member if does not exist, arg represents a member or dataset(member)
    uname                   - echo connection hostname and z/OS version
    usermod <arg>           - modify username or password of current connection, arg can be either -u or -p
    vi <arg>                - arg is a member, sequential dataset, or dataset(member), arg will be downloaded
    whoami                  - current connection's username  
  
Along with the following custom commands:  

    bj | browsejob <arg1> <arg2>        - arg1=task/job name; display job's JESMSGLG spool output; arg2=optional or "all"; all=display all job's spool output
    cancel <arg>                        - arg=task/job name; cancel  
    change <arg>                        - arg=connection number, ordered connections defined in config.json
    color <arg> <arg2>                  - change color arg is prompt and text and arg2 is background color, i.e. blue, yellow, cyan etc..
    connections                         - connection(s) list from config.json   
    count members                       - number of members in PWD
    count datasets                      - renumber of datasets in PWD
    d | download <arg1> <arg2>          - arg1=member or sequential dataset; download to \ZosShell\pwd; arg2=optional or -b for binary download   
    dj | downloadjob <arg1> <arg2>      - arg1=task/job name; download JESMSGLG spool output; arg2="all", download all spool content
    end                                 - exit UI shell
    exit                                - exit UI shell
    quit                                - exit UI shell
    files                               - list all files under local PWD drive value
    ls --l <arg>                        - same as ls -l without attribute info
    mvs <arg>                           - execute console command, arg=command in double quotes
    p | purge <arg>                     - arg=job name or id; purge   
    rn | rename <arg1> <arg2>           - rename member or sequential, arg1=old arg2=new
    save <arg>                          - arg=file name from files command to the current PWD
    search <arg>                        - search previous command contents  
    stop <arg>                          - arg=task/job name; stop
    submit <arg>                        - arg=task/job name; submit   
    tail <arg1> <arg2> <arg3>           - arg1=task/job name; display content from bottom; arg2 & arg3 = optional; arg2=limit num (25 default) and arg3=all spool content
    t | timeout <arg>                   - echo current timeout value or change value with arg
    tso <arg>                           - execute OMVS/USS command via SSH connection, arg=command within double quotes
    ussh <arg>                          - execute a uss (unix) command via SSH connection where arg is a command string within double quotes
    v | visited                         - list of visited datasets  
  
Key combinations provide the following functionality within the shell:    
  
All key combinations work on Windows and Mac OSX unless specified otherwise.  
      
    CTRL C                  - copy text
    CTRL V                  - paste coped text
    UP arrow                - scroll up through history list 
    DOWN arrow              - scroll down through history list
    CTRL UP arrow           - increase font size  (Windows)
    CTRL DOWN arrow         - decrease font size  (Windows)
    SHIFT UP arrow          - increase font size  (Mac OSX)
    SHIFT DOWN arrow        - decrease font size  (Mac OSX)
    TAB                     - command autofill key - type a few characters then click on TAB key 
    
To quit from the command shell UI, you can either press 'X' windows close icon or enter: end, exit, or quit keyword.  
  
## Requirements  

    Maven
    Compatible with all Java versions 11 and above.
    z/OSMF installed on your backend z/OS instance.
              
## Build And Execute  
          
At the root directory prompt, execute the following maven command:  
  
    mvn clean install  
  
Change the directory to the target directory and execute the following command:  
  
    java -jar zosshell-2.1.1.jar   
  
Since version 3.0.0, you can send an argument value to the java command above, for instance:  
  
    java -jar zosshell-2.1.1.jar 2  
  
This will load the second profile defined in config.json at startup instead of the first one, which is done by default.  
  
If you are planning to browse large job output, you may want to set the JVM memory usage higher than the default, i.e.  
  
    java -jar -Xmx2G zosshell-2.1.1.jar   
  
### Terminal configuration properties
  
By default, the configuration file name is config.json located within C:\ZosShell directory for Windows or /ZosShell for macOS.  
  
You can override the default file name and its location by setting the following OS environment variable:  
  
    ZOSSHELL_CONFIG_PATH  
  
The configuration file consists of JSON data. The configuration JSON string is defined as a JSON array structure. The array will consist of one or more profile(s).
  
A profile is a one-to-one relationship of [Profile.java](https://github.com/Zowe-Java-SDK/ZosShell/blob/master/src/main/java/zos/shell/singleton/configuration/model/Profile.java) file within the project. It contains variables as a placeholder for configuration information, such as z/OSMF and SSH connection information, properties to control the Window environment and much more.  
   
The first JSON array entry in the example below shows all the attributes defined to be read by the application.  
  
The other JSON array entries show that you don't need to specify all attributes and its values. The attributes required are those that specify a z/OSMF connection: hostname and zosmfport.    
   
The username and password entries are optional. It is recommended to not specify those settings. When not specified, the application will prompt the end user for username and password for the current connection.   
    
For further details on username and password usage, see [here](https://github.com/Zowe-Java-SDK/ZosShell/issues/182).    
    
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
            "browselimit": "",    
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
            "browselimit": "",   
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
            "browselimit": "1000",   
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
    browselimit as BROWSE_LIMIT
    prompt as PROMPT
  
Each of these environmental variables will appear at app startup via ENV command if any have a value specified within the configuration JSON file.

Use SET command to define or change each environmental variable noted above as needed, and the new settings will be used by the application accordingly.
  
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
    
The following screenshot displays the ZosShell shell window with custom windows properties defined within the "window" JSON section.  
  
Here the window is set to display its background in green and font as yellow/bold.   
  
![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demos/colors.gif)    

## Troubleshooting
    
Logging framework log4j2 is configured for the project. log4j2 configuration is located under src/main/resources/log4j2.xml.  
    
It is configured to produce output logging while the application is running under the running directory where the application was kicked off.
      
You are free to change configuration accordingly for your needs. 
  

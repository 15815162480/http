### 1.6.1
* Move settings to a Setting>Tools>ApiTool, Now it's global configuration instead of single project configuration


### 1.6.0
* improve: Move settings to a new tab
* feature: New setting-Can add a custom controller annotation(Set whether to move this annotation or not)

### 1.5.6

* feature: Supported 241.*
* feature: New setting-Can set search everywhere (Because the IDEA IU 2024.1 support for endpoint search)

### 1.5.5

* fix bugs: When requesting the addition of a header table, if the value is empty after editing, the row will be deleted

### 1.5.4

* fix bugs: After generating data in the request header table, clicking "+" will not automatically delete blank rows
  when no data is filled in
* improve: The width of the fixed number and request method columns in the request history panel, and adjust the width
  of the remaining two columns
* improve: View request history —— response tabs add selecting response body type
* improve: View request history —— remove all buttons
* fix bugs: In IDEA's new UI compact mode, when switching to the request body or response body tab, the window shakes (
  does not appear in the old UI and non-compact mode), the left border of the entire window is 1px more

### 1.5.3

* fix bugs: Request History generates error —— File must be not null

### 1.5.2

* fix bugs: At the binding area, there is wrong text when pressing alt and Enter
* fix bugs: Click on the binding area method. If the API label is not selected in the window panel label, it will not
  automatically switch to the API label page during generation
* feature: Request History(max 50 records), can regenerate selected requests

### 1.5.1

* improve: Add SearchEverywhere ApiTool tab shortcut key (ctrl \\). If the content in the clipboard starts with "/",
  the content will be automatically filled into the input box
* improve: Add ApiTool window shortcuts(ctrl shift q)

### 1.5.0

* improve: If a field is decorated with @JsonProperty, the corresponding field is automatically converted to the value
  corresponding to the @JsonProperty value attribute (the request content-type is not classified).
* fix bugs: Fix a series of errors: Read access is allowed from inside read-action
* improve: You can convert only one String constant reference to a path to the corresponding value.This is an example:
   ```java
   private static final String A = "/test";
   
   @GetMapping(A)
   public String testGet() { 
     return "test"; 
   }
   ```
* improve: supported @RequestMapping method property,
  eg:`@RequestMapping(value = "/test", method = {RequestMethod.PUT, RequestMethod.DELETE})`, will be a put request

### 1.4.10

* improve: removing Unused Code
* fix bugs: Quickly switch the selected method item to an error.
* help: In IDEA's new UI compact mode, when switching to the request body or response body tab, the window shakes (does
  not appear in the old UI and non-compact mode), the left border of the entire window is 1px more

### 1.4.9

* feature: Supported 233.*

### 1.4.8

* feature: New setting-Can set whether to refresh when switching branches (because different interfaces may exist for
  different branches)

### 1.4.7

* improve: The selected environment is not allowed to be deleted
* fix bugs: remove feign related interface binding area icon

### 1.4.6

* improve: Add request success time statistics

### 1.4.5

* fix bugs: Path generates error when no path is requested on Controller
* improve: Release the conditions for initiating a request: the node must be selected (can now initiate a request to a
  third party)

### 1.4.4

* improve:If the root module is not linked (that is, the project is not introduced, for example, maven does not
  reference the root module or the artifactId in pom is inconsistent with the project name), the project name will be
  used as the root directory(The interfaces are all in the module and need to be introduced).

### 1.4.3

* fix bugs: Request path generation error in specific cases
* feature: Support file upload, Only a single parameter `void a(MultipartFile[] file)` or
  `void a(MultipartFile file)` is supported, and the file upload method with two parameters
  such as `void a(MultipartFile[] file, MultipartFile[] file2)` is not supported (no suitable
  UI is found for display)

### 1.4.2

* fix bugs: Collection or array generate error data
* fix bugs: Spring mvc param type: ModelMap and ModelAndView generate error data
* fix bugs: The newly written request method clicks the icon on the left and no data is generated

### 1.4.1

* improve: Special treatment of enumeration parameters, only enumeration values are displayed
* fix bugs: Exception in editing request header feature
* fix bugs: Whether the binding icon area is set to display, the icon is missing
* fix bugs: When the selected environment configuration is edited, the host value and request header will not change.

### 1.4.0

* feature: Add a "ApiTool" tab into Search Everywhere, api can be matched based on / split
* feature: Add an icon to the left of the request method (officially called the binding area), click to generate the
  corresponding request and expand the tree to the corresponding node
* feature: Header and Param can edit as a properties
* fix bugs: Toolwindow theme do not refresh synchronously when switching themes
* improve: Adapt to light color mode(Mainly new UI)
* fix bugs: When the node is selected, the environment is switched, and the request header does not change accordingly.
* fix bugs: API tab add environment function exception

### 1.3.0

* fix bugs: Refresh does not clear the request header, parameters, request body, and return body.
* improve: Request body support Generics Type(Do not nest parameters with each other, Will cause Stack Overflow)
* fix bugs: "java.lang.Boolean" generates error value

### 1.2.3

* improve: Move "Env List" to new tab

### 1.2.2

* feature: Can set whether to generate a default environment and remove the default environment
* improve: Modify the default port when there is no "server.port" to 8080
* improve: Integration of node filtering functions
* fix bugs: export postman data encoding error
* fix bugs: export postman data protocol, host, path lost
* improve: Change the window name to ApiTool

### 1.2.1

* feature: Ability to choose whether to show package nodes or class nodes
* feature: Response now can be viewed in a dialog, but edit it will not echo
* fix bugs: Remove request header and prohibit duplication
* fix bugs: No method node selected can still initiate a request

### 1.2.0

* feature: Support for exporting postman environment configuration, api list
* feature: Expand and collapse function optimization, if there is selected node, only the selected nodes will be
  expanded or collapse.
* feature: Module Node, Package Node, Class Node add the right-click menu: expand all child node
* fix bugs: Send a new request, the older request do not clear the response editor

### 1.1.0

* feature: Request Method Filter
* feature: Method node add right-click menu: Jump to Method, Copy Full Path, Copy Api
* feature: Request body editor can open in new dialog to edit
* fix bugs: change env does not change the host value, header and so on
* fix bugs: When the selected environment configuration is edited, the host value and request header will not change.

### 1.0.2

* fix bugs: Package and class are at the same level, class node is missing
* fix bugs: choose a new request, the older request do not clear the response editor

### 1.0.1

* Change plugin name —— ApiTool

### 1.0.0

* feature: Read all annotation request interface methods in Spring MVC
* Multi Environment configuration, by default, reads the context-path and port of each module and generates a local
  environment for each module (refresh will still take effect after deletion)
* If the project refers to Swagger2 and Swagger3 annotations, hover the mouse over the corresponding node will display
  the corresponding description
* Double-click the method node to jump to the specified method


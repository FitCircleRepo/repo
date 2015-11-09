There are a few known issues with our code which cause memory leaks and problems while undeploying/re-loading our web apps.

We will be implementing fixes for the same in the next release(s) of our bots.

Proposed Approach -
	*) Not using a  a static TeamchatAPI object
	*) Initializing TeamchatAPI object using a Web Listener (On ContextInitialized) instead of the servlet's init() method
	*) Using Schedulers on ContextInitialized instead of the servlet's init() method
	*) Using the Model-View-Controller architecture (aiming to store Bot Credentials in external files on disk instead of within the project itself, same goes for loading filepaths/URLs from files wherever possible/ necessary)
	*) More efficient Object initializations, Exception Handling and Log file generation/logging practises.


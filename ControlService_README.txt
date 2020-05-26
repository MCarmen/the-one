The ONE v1.6.0 with a Control Service
=====================================
This project is a fork of the akeranen/the-one project.
We have added a control service which is used to have an overview of the 
status of the opportunistic network. 
To implement the control service, several control sub-packages have been 
added to the already existing packages. The class routing.MessageRouter 
has been modified.
The control service is set and configured through the the-one settings file. 
If the control service is off, the simulator works as the original one. 

This project is part of a PhD thesis supervised by Dr Carlos Borrego Iglesias 
from the Information and Communications Engineering Department at 
Universitat Autònoma de Barcelona.


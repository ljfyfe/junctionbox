# JunctionBox
JunctionBox is an interaction design and mapping toolkit for building multi-touch audio control interfaces. The toolkit allows developers to build interfaces for TUIO-based touch tables and Android mobile devices, including phones and tablets. Multi-touch interactions on those devices can then be mapped to control audio engines via Open Sound Control (OSC) messaging. Example audio engines that work with JunctionBox include ChucK, PureData, and SuperCollider.

JunctionBox provides Juctions, or interactive parts of an interface, that represent a convergence of touch input, graphical output, and OSC message output. Junctions can be manipluated in a variety of ways (translation, rotation, scaling, etc.) and these manipulations are then mapped to control of sound music.

Features:

  * Supports multi-touch input from TUIO and Android devices as well as mouse input
  * Automatically handles mapping of multi-touch input to Open Sound Control (OSC) messages
  * Has 20 different multi-touch interactions that are individually mappable to OSC messages
  * Maps multi-touch input to graphical output via Processing
  * Supports networking on either local area networks (LANs) or over the internet
  * Saves the current interaction state of an interface and allows that state to be loaded at a later time
  * Allows interactions can be inherited between interface objects
  * Supports the recording and playback of interactions
  * Implements the Nexus Data Exchange Format (NDEF) specification for connecting and managing messages between different devices 

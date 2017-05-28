# Inlein News – history of user-visible changes

## 0.2.0 / 2017-05-28

* Added support for `:file-deps` (Jean Niklas L'orange)
* Added support for `:exclusions` in the parameter map (Jean Niklas L'orange)
* Fixed a bug where the Windows daemon weren't properly daemonised (Jean Niklas L'orange)
* Fixed a bug where Inlein crashed when port files weren't properly cleaned up (Mark Mathis)
* Added proxy support via the System properties `http.proxy{Port,Host}` (Vladimir Kadychevski)
* Fixed a bug where download failed on machines where `/tmp` and `$HOME` were on
  different mountpoints (Reid McKenzie)

## 0.1.0 / 2016-03-13

* First version!

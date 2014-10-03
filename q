[1mdiff --git a/webz/src/main/java/org/terems/webz/impl/WebzFileSystemManager.java b/webz/src/main/java/org/terems/webz/impl/WebzFileSystemManager.java[m
[1mindex 003afb9..962e081 100644[m
[1m--- a/webz/src/main/java/org/terems/webz/impl/WebzFileSystemManager.java[m
[1m+++ b/webz/src/main/java/org/terems/webz/impl/WebzFileSystemManager.java[m
[36m@@ -22,9 +22,8 @@[m [mpublic class WebzFileSystemManager extends BaseWebzDestroyable {[m
[m
	public WebzFileSystem createFileSystem(Properties properties) throws WebzException {[m
[m
[31mGenericWebzFileSystem fileSystem = factory.newDestroyable(GenericWebzFileSystem.class);[m		return [31mfileSystem.init(properties[m[32mfactory.newDestroyable(GenericWebzFileSystem.class)[m
[32m				.init(properties[m == null ? null : new WebzProperties(properties), factory);
	}[m
[m
}[m

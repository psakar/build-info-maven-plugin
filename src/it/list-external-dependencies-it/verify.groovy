File file = new File( basedir, "target/dependencies_external_all.txt" );
File expectedFile = new File( basedir, "src/test/expected/dependencies_external_all.txt" );

List<String> lines = file.readLines();
List<String> expectedLines = expectedFile.readLines();
assert lines.size == expectedLines.size
assert file.getText() == expectedFile.getText()

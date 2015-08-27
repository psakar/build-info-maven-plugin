File file = new File( basedir, "target/dependencies.txt" );
File expectedFile = new File( basedir, "src/test/expected/dependencies.txt" );

List<String> lines = file.readLines();
List<String> expectedLines = expectedFile.readLines();
assert lines.size == expectedLines.size
assert file.getText() == expectedFile.getText()

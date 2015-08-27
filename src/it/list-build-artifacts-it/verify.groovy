File file = new File( basedir, "target/build_artifacts.txt" );
File expectedFile = new File( basedir, "src/test/expected/build_artifacts.txt" );

List<String> lines = file.readLines();
List<String> expectedLines = expectedFile.readLines();
assert lines.size == expectedLines.size
assert file.getText() == expectedFile.getText()
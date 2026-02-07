import xml.etree.ElementTree as ET
import glob
import fnmatch

EXCLUSIONS = [
    "**/config/**", "**/model/**", "**/security/**", "**/exception/**", 
    "**/*Application.java", "**/dto/**", "**/src/test/**"
]

def is_excluded(package_name, source_file):
    # Convert package name to path format for matching
    path = package_name.replace('.', '/') + '/' + source_file
    for exclusion in EXCLUSIONS:
        # Simple glob matching
        pattern = exclusion.replace("**/", "*").replace("**", "*")
        if fnmatch.fnmatch(path, pattern) or fnmatch.fnmatch(package_name, pattern.strip("/*")):
            return True
        if "dto" in package_name or "model" in package_name or "config" in package_name or "exception" in package_name or "security" in package_name:
             return True
        if "Application.java" in source_file:
             return True
    return False

def parse_coverage():
    total_missed = 0
    total_covered = 0
    
    files = glob.glob("services/*/target/site/jacoco/jacoco.xml")
    if not files:
        print("No jacoco.xml files found")
        return

    for file in files:
        try:
            tree = ET.parse(file)
            root = tree.getroot()
            
            file_missed = 0
            file_covered = 0

            for package in root.findall("package"):
                package_name = package.get("name")
                
                for sourcefile in package.findall("sourcefile"):
                    source_name = sourcefile.get("name")
                    
                    if is_excluded(package_name, source_name):
                        continue

                    for counter in sourcefile.findall("counter"):
                        if counter.get("type") == "INSTRUCTION":
                            missed = int(counter.get("missed"))
                            covered = int(counter.get("covered"))
                            file_missed += missed
                            file_covered += covered
            
            total_missed += file_missed
            total_covered += file_covered
            
            if file_missed + file_covered > 0:
                print(f"{file}: Coverage={file_covered/(file_covered+file_missed)*100:.1f}% (Covered={file_covered}, Total={file_covered+file_missed})")
                # Print breakdown for this service if coverage is low
                if file_covered/(file_covered+file_missed) < 0.8:
                     print("  Low coverage files:")
                     for package in root.findall("package"):
                        package_name = package.get("name")
                        for sourcefile in package.findall("sourcefile"):
                            source_name = sourcefile.get("name")
                            if is_excluded(package_name, source_name):
                                continue
                            
                            sf_missed = 0
                            sf_covered = 0
                            for counter in sourcefile.findall("counter"):
                                if counter.get("type") == "INSTRUCTION":
                                    sf_missed += int(counter.get("missed"))
                                    sf_covered += int(counter.get("covered"))
                            
                            if sf_missed + sf_covered > 0:
                                sf_cov = sf_covered / (sf_covered + sf_missed) * 100
                                if sf_cov < 80:
                                    print(f"    {source_name}: {sf_cov:.1f}% ({sf_covered}/{sf_covered+sf_missed})")

            else:
                 print(f"{file}: Coverage=N/A (Filtered out)")

        except Exception as e:
            print(f"Error parsing {file}: {e}")

    if total_covered + total_missed > 0:
        total_coverage = (total_covered / (total_covered + total_missed)) * 100
        print(f"\nTotal Filtered Coverage: {total_coverage:.1f}%")
    else:
        print("\nTotal Filtered Coverage: 0%")

if __name__ == "__main__":
    parse_coverage()

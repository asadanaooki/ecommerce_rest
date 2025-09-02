#!/usr/bin/env python3
import os
import re
from pathlib import Path

def extract_todos_from_file(filepath):
    """Extract all TODO comments from a single file."""
    todos = []
    
    try:
        with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
            lines = content.split('\n')
            
        i = 0
        while i < len(lines):
            line = lines[i]
            
            # Check for single-line TODO comment
            if 'TODO' in line:
                # Handle block comment TODO
                if '/*' in line and 'TODO' in line:
                    todo_lines = []
                    # Find the start
                    todo_lines.append(line)
                    
                    # If */ is not on the same line, keep reading
                    if '*/' not in line:
                        i += 1
                        while i < len(lines):
                            todo_lines.append(lines[i])
                            if '*/' in lines[i]:
                                break
                            i += 1
                    
                    todos.append({
                        'line_num': i - len(todo_lines) + 2,  # 1-based line number
                        'content': '\n'.join(todo_lines)
                    })
                
                # Handle single-line // TODO or -- TODO
                elif '//' in line and 'TODO' in line:
                    todo_lines = [line]
                    start_line = i + 1
                    
                    # Check if next lines are continuation comments
                    i += 1
                    while i < len(lines):
                        next_line = lines[i].strip()
                        if next_line.startswith('//'):
                            todo_lines.append(lines[i])
                            i += 1
                        else:
                            i -= 1  # Back up one since we're not consuming this line
                            break
                    
                    todos.append({
                        'line_num': start_line,
                        'content': '\n'.join(todo_lines)
                    })
                
                elif '--' in line and 'TODO' in line:
                    todos.append({
                        'line_num': i + 1,
                        'content': line
                    })
            
            i += 1
    
    except Exception as e:
        print(f"Error reading {filepath}: {e}")
    
    return todos

def get_class_name(filepath):
    """Extract the main class/interface name from a Java file."""
    try:
        with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
            
        # Look for public class or interface
        match = re.search(r'public\s+(?:class|interface|enum)\s+(\w+)', content)
        if match:
            return match.group(1)
    except:
        pass
    
    # Fallback to filename
    return Path(filepath).stem

def main():
    # Find all source files
    source_extensions = ['.java', '.sql', '.xml', '.properties', '.yml', '.yaml', '.js', '.ts', '.jsx', '.tsx', '.css', '.scss', '.html']
    
    all_todos = []
    
    for root, dirs, files in os.walk('.'):
        # Skip target, node_modules, .git directories
        dirs[:] = [d for d in dirs if d not in ['target', 'node_modules', '.git', '.mvn']]
        
        for file in files:
            if any(file.endswith(ext) for ext in source_extensions):
                filepath = os.path.join(root, file)
                todos = extract_todos_from_file(filepath)
                
                if todos:
                    # Get relative path for cleaner output
                    rel_path = os.path.relpath(filepath, '.')
                    rel_path = rel_path.replace('\\', '/')  # Normalize path separators
                    
                    # Get class name for Java files
                    class_name = ""
                    if filepath.endswith('.java'):
                        class_name = get_class_name(filepath)
                    
                    all_todos.append({
                        'file': rel_path,
                        'class': class_name,
                        'todos': todos
                    })
    
    # Write TODO_LIST.txt
    with open('TODO_LIST.txt', 'w', encoding='utf-8') as out:
        out.write("=" * 80 + "\n")
        out.write("TODO LIST - Complete Repository Scan\n")
        out.write("=" * 80 + "\n\n")
        
        for file_todos in sorted(all_todos, key=lambda x: x['file']):
            out.write("-" * 80 + "\n")
            out.write(f"File: {file_todos['file']}\n")
            if file_todos['class']:
                out.write(f"Class: {file_todos['class']}\n")
            out.write("-" * 80 + "\n\n")
            
            for todo in file_todos['todos']:
                out.write(f"Line {todo['line_num']}:\n")
                out.write(todo['content'] + "\n")
                out.write("\n")
            
            out.write("\n")
    
    print(f"TODO extraction complete. Found {sum(len(f['todos']) for f in all_todos)} TODOs in {len(all_todos)} files.")
    print("Results saved to TODO_LIST.txt")

if __name__ == "__main__":
    main()
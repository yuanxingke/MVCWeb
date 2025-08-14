#!/usr/bin/env python3
"""
Test runner script for MVCWeb API
Supports both unittest and pytest execution
"""

import sys
import subprocess
import os

def run_unittest():
    """Run tests using unittest"""
    print("Running tests with unittest...")
    print("=" * 50)
    
    # Run unittest with verbose output
    result = subprocess.run([
        sys.executable, '-m', 'unittest', 'test_app.py', '-v'
    ], capture_output=False)
    
    return result.returncode == 0

def run_pytest():
    """Run tests using pytest with coverage"""
    print("Running tests with pytest...")
    print("=" * 50)
    
    # Check if pytest is available
    try:
        import pytest
        import pytest_cov
    except ImportError:
        print("pytest or pytest-cov not installed. Install with:")
        print("pip install pytest pytest-cov")
        return False
    
    # Run pytest with coverage
    result = subprocess.run([
        sys.executable, '-m', 'pytest', 'test_app.py'
    ], capture_output=False)
    
    return result.returncode == 0

def main():
    """Main test runner"""
    if len(sys.argv) > 1 and sys.argv[1] == 'pytest':
        success = run_pytest()
    else:
        success = run_unittest()
        
        # Also try pytest if available
        print("\n" + "=" * 50)
        print("Also running with pytest (if available)...")
        run_pytest()
    
    if success:
        print("\n✅ All tests passed!")
        return 0
    else:
        print("\n❌ Some tests failed!")
        return 1

if __name__ == '__main__':
    sys.exit(main())
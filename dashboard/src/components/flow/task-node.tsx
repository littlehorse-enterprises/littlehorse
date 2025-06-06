import React from 'react';
import { Handle, Position } from '@xyflow/react';

interface TaskNodeProps {
    data: {
        label: string;
        status?: 'pending' | 'running' | 'completed' | 'failed';
    };
}

export default function TaskNode({ data }: TaskNodeProps) {
    const getStatusColor = (status?: string) => {
        switch (status) {
            case 'running':
                return 'bg-yellow-100 border-yellow-500 text-yellow-700';
            case 'completed':
                return 'bg-green-100 border-green-500 text-green-700';
            case 'failed':
                return 'bg-red-100 border-red-500 text-red-700';
            default:
                return 'bg-gray-100 border-gray-500 text-gray-700';
        }
    };

    return (
        <div className={`px-4 py-2 shadow-md rounded-md border-2 ${getStatusColor(data.status)}`}>
            <Handle
                type="target"
                position={Position.Left}
                className="w-2 h-2 !bg-gray-400"
            />
            <div className="font-medium text-sm">
                {data.label}
            </div>
            <Handle
                type="source"
                position={Position.Right}
                className="w-2 h-2 !bg-gray-400"
            />
        </div>
    );
} 
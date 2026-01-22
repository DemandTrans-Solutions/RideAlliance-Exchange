
export class UserInfo {
accountDisabled: boolean;
accountExpired: boolean;
accountLocked: boolean;
authanticationTypeIsAdapter: boolean;
authorities: Array<Authority>;
credentialsExpired: boolean;
csrfToken: string;
expires: string;
id: number;
jobTitle: string;
name: string;
username: string;
responseDataForUI: number;
isPasswordExpired: any;
}

export class Authority {
authority: string;
}
